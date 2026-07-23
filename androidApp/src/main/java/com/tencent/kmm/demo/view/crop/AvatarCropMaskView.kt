package com.tencent.kmm.demo.view.crop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 头像裁剪蒙层，参考 Android 端头像裁剪页的暗色圆形选框。
 */
class AvatarCropMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#B3000000")
    }
    private val maskPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!maskPath.isEmpty) {
            canvas.drawPath(maskPath, maskPaint)
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        maskPath.reset()
        if (width <= 0 || height <= 0) {
            return
        }

        val cropRect = calculateCropRect(width, height, resources.displayMetrics.density)
        maskPath.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
        maskPath.addRoundRect(
            cropRect,
            cropRect.width() / 2f,
            cropRect.height() / 2f,
            Path.Direction.CCW,
        )
        maskPath.fillType = Path.FillType.WINDING
    }

    companion object {
        private const val MASK_MARGIN_DP = 16

        fun calculateCropRect(width: Int, height: Int, density: Float = 1f): RectF {
            val margin = MASK_MARGIN_DP * density
            return if (width < height) {
                val left = margin
                val right = width - margin
                val cropWidth = right - left
                val top = (height - cropWidth) / 2f
                RectF(left, top, right, top + cropWidth)
            } else {
                val left = (width - height) / 2f
                RectF(left, 0f, left + height, height.toFloat())
            }
        }
    }
}
