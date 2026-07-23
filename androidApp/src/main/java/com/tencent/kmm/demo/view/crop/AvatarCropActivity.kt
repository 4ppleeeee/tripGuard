package com.tencent.kmm.demo.view.crop

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tencent.news.core.page.model.StructSize.Companion.WRAP_CONTENT
import com.tencent.kmm.demo.library.log.WsLogger
import java.io.File

/**
 * 头像裁剪页面，参考 Android 端头像裁剪页的全屏裁剪交互。
 */
class AvatarCropActivity : AppCompatActivity() {

    private lateinit var cropImageView: AvatarCropImageView
    private lateinit var revertButton: TextView

    private var sourceFromCamera = false
    private var outputPath = ""
    private var outputSize = DEFAULT_OUTPUT_SIZE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        sourceFromCamera = intent.getBooleanExtra(EXTRA_SOURCE_CAMERA, false)
        outputPath = intent.getStringExtra(EXTRA_OUTPUT_PATH).orEmpty()
        outputSize = intent.getIntExtra(EXTRA_OUTPUT_SIZE, DEFAULT_OUTPUT_SIZE)
        if (outputPath.isBlank()) {
            WsLogger.e(TAG, "Output path is empty")
            finishWithCancel()
            return
        }

        setContentView(createContentView())
        val sourceUri = intent.data
        if (sourceUri == null) {
            WsLogger.e(TAG, "Source uri is null")
            finishWithCancel()
            return
        }
        cropImageView.setImageUri(sourceUri, outputSize) { error ->
            WsLogger.e(TAG, "Set crop image uri failed", error)
            finishWithCancel()
        }
    }

    private fun createContentView(): View {
        return FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            cropImageView = AvatarCropImageView(this@AvatarCropActivity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            addView(cropImageView)
            addView(
                AvatarCropMaskView(this@AvatarCropActivity),
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
            addView(createBottomBar())
        }
    }

    private fun createBottomBar(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dpToPx(BOTTOM_HORIZONTAL_PADDING_DP),
                0,
                dpToPx(BOTTOM_HORIZONTAL_PADDING_DP),
                0,
            )
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(BOTTOM_BAR_HEIGHT_DP),
                Gravity.BOTTOM,
            )
            addView(createIconButton("×") { finishWithCancel() })
            addView(createSpacer())
            revertButton = createTextButton("还原", false) {
                cropImageView.revert { error ->
                    WsLogger.e(TAG, "Revert crop image failed", error)
                    finishWithCancel()
                }
                updateRevertButton(false)
            }
            addView(revertButton)
            addView(createSpacer())
            addView(createTextButton("旋转", true) {
                cropImageView.rotate90()
                updateRevertButton(cropImageView.getCurrentRotateDegree() != 0)
            })
            addView(createSpacer())
            addView(createIconButton("✓") { saveCropResult() })
        }
    }

    private fun createSpacer(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        }
    }

    private fun createIconButton(text: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            textSize = 30f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT,
            )
            setOnClickListener { onClick() }
        }
    }

    private fun createTextButton(text: String, enabled: Boolean, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(if (enabled) Color.WHITE else Color.parseColor("#B3FFFFFF"))
            isEnabled = enabled
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setOnClickListener { onClick() }
        }
    }

    private fun updateRevertButton(enabled: Boolean) {
        revertButton.isEnabled = enabled
        revertButton.setTextColor(if (enabled) Color.WHITE else Color.parseColor("#B3FFFFFF"))
    }

    private fun saveCropResult() {
        val isSuccess = cropImageView.crop(outputPath, outputSize)
        if (!isSuccess || !File(outputPath).exists()) {
            WsLogger.e(TAG, "Crop avatar failed")
            return
        }
        setResult(RESULT_OK, Intent().setData(Uri.fromFile(File(outputPath))))
        finish()
    }

    private fun finishWithCancel() {
        setResult(
            RESULT_CANCELED,
            Intent().putExtra(EXTRA_SOURCE_CAMERA, sourceFromCamera),
        )
        finish()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    companion object {
        const val EXTRA_OUTPUT_PATH = "extra_output_path"
        const val EXTRA_OUTPUT_SIZE = "extra_output_size"
        const val EXTRA_SOURCE_CAMERA = "extra_source_camera"

        private const val TAG = "AvatarCropActivity"
        private const val DEFAULT_OUTPUT_SIZE = 512
        private const val BOTTOM_BAR_HEIGHT_DP = 69
        private const val BOTTOM_HORIZONTAL_PADDING_DP = 12
        private const val ICON_BUTTON_SIZE_DP = 30
    }
}
