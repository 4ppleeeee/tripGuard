package com.tencent.kmm.demo.view.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import androidx.exifinterface.media.ExifInterface
import com.tencent.kmm.demo.library.log.WsLogger
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 头像裁剪图片视图，提供拖拽、缩放、旋转和输出裁剪图能力。
 */
class AvatarCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val drawMatrix = Matrix()
    private val matrixValues = FloatArray(MATRIX_VALUE_SIZE)
    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val bitmap = bitmap ?: return false
                val cropRect = cropRect.takeIf { !it.isEmpty } ?: return false
                val currentScale = getCurrentScale()
                val targetScale = (currentScale * detector.scaleFactor).coerceIn(minScale, maxScale)
                val scaleFactor = targetScale / currentScale
                drawMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                ensureImageCoversCropRect(bitmap, cropRect)
                invalidate()
                return true
            }
        },
    )

    private var sourceUri: Uri? = null
    private var bitmap: Bitmap? = null
    private var cropRect = RectF()
    private var minScale = 1f
    private var maxScale = 4f
    private var rotateDegree = 0
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private var imageLoadScope = createImageLoadScope()
    private var imageLoadJob: Job? = null
    private var imageLoadRequestId = 0L
    private var decodeOutputSize = DEFAULT_DECODE_OUTPUT_SIZE

    override fun onDraw(canvas: Canvas) {
        val bitmap = bitmap ?: return
        canvas.drawBitmap(bitmap, drawMatrix, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && !scaleDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    drawMatrix.postTranslate(dx, dy)
                    bitmap?.let { ensureImageCoversCropRect(it, cropRect) }
                    invalidate()
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                isDragging = false
            }
        }
        return true
    }

    fun setImageUri(uri: Uri) {
        setImageUri(uri, DEFAULT_DECODE_OUTPUT_SIZE)
    }

    fun setImageUri(
        uri: Uri,
        outputSize: Int,
        onFailure: (Throwable) -> Unit = {},
    ) {
        sourceUri = uri
        decodeOutputSize = outputSize.coerceAtLeast(DEFAULT_DECODE_OUTPUT_SIZE)
        rotateDegree = 0
        loadBitmapAsync(uri, decodeOutputSize, onFailure)
    }

    fun rotate90() {
        rotateDegree = (rotateDegree + ROTATE_DEGREE_STEP) % FULL_ROTATE_DEGREE
        bitmap = bitmap?.rotate(ROTATE_DEGREE_STEP)
        resetImageMatrix()
    }

    fun revert() {
        revert {}
    }

    fun revert(onFailure: (Throwable) -> Unit = {}) {
        val uri = sourceUri ?: return
        rotateDegree = 0
        loadBitmapAsync(uri, decodeOutputSize, onFailure)
    }

    fun getCurrentRotateDegree(): Int = rotateDegree

    fun crop(outputPath: String, outputSize: Int, quality: Int = JPEG_QUALITY): Boolean {
        val bitmap = bitmap ?: return false
        val cropRect = cropRect.takeIf { !it.isEmpty } ?: return false
        return runCatching {
            val outputBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outputBitmap)
            val cropMatrix = Matrix(drawMatrix)
            cropMatrix.postTranslate(-cropRect.left, -cropRect.top)
            val scale = outputSize / cropRect.width()
            cropMatrix.postScale(scale, scale)
            canvas.drawBitmap(bitmap, cropMatrix, drawPaint)

            File(outputPath).parentFile?.mkdirs()
            FileOutputStream(outputPath).use { output ->
                outputBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            }
            outputBitmap.recycle()
            true
        }.getOrDefault(false)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        cropRect = AvatarCropMaskView.calculateCropRect(width, height, resources.displayMetrics.density)
        resetImageMatrix()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        imageLoadJob?.cancel()
        imageLoadScope.cancel()
        recycleBitmap()
    }

    private fun loadBitmapAsync(
        uri: Uri,
        outputSize: Int,
        onFailure: (Throwable) -> Unit,
    ) {
        imageLoadJob?.cancel()
        val requestId = ++imageLoadRequestId
        recycleBitmap()
        drawMatrix.reset()
        invalidate()
        imageLoadJob = imageLoadScope.launch {
            try {
                val targetSize = calculateDecodeTargetSize(outputSize)
                val decodedBitmap = withContext(Dispatchers.IO) {
                    decodeSampledBitmap(uri, targetSize)
                }
                if (requestId != imageLoadRequestId) {
                    decodedBitmap.recycle()
                    return@launch
                }
                bitmap = decodedBitmap
                resetImageMatrix()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                if (requestId == imageLoadRequestId) {
                    onFailure(error)
                }
            }
        }
    }

    private fun decodeSampledBitmap(uri: Uri, targetSize: Int): Bitmap {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        }
        val imageWidth = boundsOptions.outWidth
        val imageHeight = boundsOptions.outHeight
        if (imageWidth <= 0 || imageHeight <= 0) {
            error("Invalid crop image bounds")
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(imageWidth, imageHeight, targetSize)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: error("Decode crop image failed")

        return applyExifOrientation(decodedBitmap, readExifOrientation(uri))
    }

    private fun calculateDecodeTargetSize(outputSize: Int): Int {
        val displayMetrics = resources.displayMetrics
        val viewTargetSize = when {
            !cropRect.isEmpty -> max(cropRect.width(), cropRect.height()).toInt()
            width > 0 && height > 0 -> max(width, height)
            else -> min(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
        return (max(viewTargetSize, outputSize) * DECODE_QUALITY_MULTIPLIER)
            .coerceIn(MIN_DECODE_TARGET_SIZE, MAX_DECODE_TARGET_SIZE)
    }

    private fun calculateInSampleSize(imageWidth: Int, imageHeight: Int, targetSize: Int): Int {
        var inSampleSize = 1
        val maxImageSize = max(imageWidth, imageHeight)
        while (maxImageSize / inSampleSize > targetSize) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun readExifOrientation(uri: Uri): Int {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.onFailure { error ->
            WsLogger.e(TAG, "Read image exif orientation failed", error)
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        val orientedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (orientedBitmap != bitmap) {
            bitmap.recycle()
        }
        return orientedBitmap
    }

    private fun resetImageMatrix() {
        val bitmap = bitmap ?: return
        if (width <= 0 || height <= 0 || cropRect.isEmpty) {
            invalidate()
            return
        }
        drawMatrix.reset()
        val imageWidth = bitmap.width.toFloat()
        val imageHeight = bitmap.height.toFloat()
        minScale = max(cropRect.width() / imageWidth, cropRect.height() / imageHeight)
        maxScale = minScale * MAX_SCALE_FACTOR
        val dx = cropRect.centerX() - imageWidth * minScale / 2f
        val dy = cropRect.centerY() - imageHeight * minScale / 2f
        drawMatrix.postScale(minScale, minScale)
        drawMatrix.postTranslate(dx, dy)
        invalidate()
    }

    private fun ensureImageCoversCropRect(bitmap: Bitmap, cropRect: RectF) {
        if (cropRect.isEmpty) {
            return
        }
        val imageRect = getImageRect(bitmap)
        val currentScale = getCurrentScale()
        if (imageRect.width() < cropRect.width() || imageRect.height() < cropRect.height()) {
            val scale = max(
                cropRect.width() / imageRect.width(),
                cropRect.height() / imageRect.height(),
            )
            drawMatrix.postScale(scale, scale, cropRect.centerX(), cropRect.centerY())
        }

        val adjustedRect = getImageRect(bitmap)
        val dx = when {
            adjustedRect.left > cropRect.left -> cropRect.left - adjustedRect.left
            adjustedRect.right < cropRect.right -> cropRect.right - adjustedRect.right
            else -> 0f
        }
        val dy = when {
            adjustedRect.top > cropRect.top -> cropRect.top - adjustedRect.top
            adjustedRect.bottom < cropRect.bottom -> cropRect.bottom - adjustedRect.bottom
            else -> 0f
        }
        drawMatrix.postTranslate(dx, dy)
        if (currentScale < minScale) {
            drawMatrix.postScale(
                minScale / currentScale,
                minScale / currentScale,
                cropRect.centerX(),
                cropRect.centerY(),
            )
        }
    }

    private fun getImageRect(bitmap: Bitmap): RectF {
        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        drawMatrix.mapRect(rect)
        return rect
    }

    private fun getCurrentScale(): Float {
        drawMatrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X].coerceAtLeast(MIN_POSITIVE_SCALE)
    }

    private fun Bitmap.rotate(degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        if (rotated != this) {
            recycle()
        }
        return rotated
    }

    private fun recycleBitmap() {
        bitmap?.recycle()
        bitmap = null
    }

    private fun createImageLoadScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    companion object {
        private const val TAG = "AvatarCropImageView"
        private const val MATRIX_VALUE_SIZE = 9
        private const val MAX_SCALE_FACTOR = 4f
        private const val MIN_POSITIVE_SCALE = 0.01f
        private const val ROTATE_DEGREE_STEP = 90
        private const val FULL_ROTATE_DEGREE = 360
        private const val JPEG_QUALITY = 80
        private const val DEFAULT_DECODE_OUTPUT_SIZE = 512
        private const val MIN_DECODE_TARGET_SIZE = 1024
        private const val MAX_DECODE_TARGET_SIZE = 2048
        private const val DECODE_QUALITY_MULTIPLIER = 2
    }
}
