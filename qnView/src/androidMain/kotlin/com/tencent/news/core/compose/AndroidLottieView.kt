package com.tencent.news.core.compose

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.news.core.compose.view.ProgressRange
import com.tencent.news.core.compose.view.QnLottieData
import com.tencent.news.core.compose.view.QnLottieScaleType
import com.tencent.news.core.extension.isNotNullOrEmpty

abstract class AndroidLottieView(context: Context) : FrameLayout(context), IKuiklyRenderViewExport {

    private var mLottieName: String? = null
    private var mLottieStatus: String? = null

    override fun setProp(propKey: String, propValue: Any): Boolean {
        when (propKey) {
            "data" -> {
                val data = propValue as? QnLottieData ?: return false
                setLottieName(data.name, data.status)
                if (mLottieName != data.name || mLottieStatus != data.status) {
                    cancelLastAnimation()
                    setProgress(0F)
                }
                mLottieName = data.name
                mLottieStatus = data.status

                data.progressRange?.let { setProgressRange(it) }
                setAutoPlay(data.autoPlay)
                setAspectRatio(data.aspectRatio)
                setInfinity(data.loop)
                if (data.tintColor.isNotNullOrEmpty()) {
                    setColorFilter("#${data.tintColor}")
                }
                data.scaleType?.let {
                    setScaleType(it)
                }
                data.scale?.let {
                    setScale(it)
                }
                data.cornerInDp?.let {
                    setCorner(it)
                }
                setTextDelegate(data.textDelegate)
            }

            "applyTheme" -> applyTheme(propValue as Boolean)
            "setProgress" -> setProgress(propValue as Float)
            "setLottieDownloadStatusListener" -> setDownloadCallback(propValue as? (String) -> Unit)
            else -> return super.setProp(propKey, propValue)
        }
        return true
    }

    final override fun onAddToParent(parent: ViewGroup) {
        super.onAddToParent(parent)
        onAttachedToWindow(parent)
    }

    final override fun onRemoveFromParent(parent: ViewGroup) {
        super.onRemoveFromParent(parent)
        onDetachedFromWindow(parent)
    }

    /**
     * 设置lottie名字或url
     */
    abstract fun setLottieName(name: String, status: String)

    /**
     * 取消上次播放的动画
     */
    abstract fun cancelLastAnimation()

    /**
     * 设置是否自动播放
     */
    abstract fun setAutoPlay(autoPlay: Boolean)

    /**
     * 设置宽高比
     */
    abstract fun setAspectRatio(aspectRatio: Float)

    /**
     * 设置是否循环播
     */
    abstract fun setInfinity(infinity: Boolean)

    abstract fun applyTheme(isDark: Boolean)

    abstract fun setProgress(progress: Float)

    abstract fun onAttachedToWindow(parent: ViewGroup)

    abstract fun onDetachedFromWindow(parent: ViewGroup)

    abstract fun setColorFilter(color: String)

    abstract fun setScaleType(scaleType: QnLottieScaleType)

    abstract fun setScale(scale: Float)

    abstract fun setCorner(cornerInDp: Float)

    abstract fun setProgressRange(range: ProgressRange)

    abstract fun setTextDelegate(textDelegate: Map<String, String>?)

    abstract fun setDownloadCallback(listener: ((String) -> Unit)?)

    fun QnLottieScaleType.mapToImageScaleType(): ImageView.ScaleType {
        return when (this) {
            QnLottieScaleType.MATRIX -> ImageView.ScaleType.MATRIX
            QnLottieScaleType.FIT_XY -> ImageView.ScaleType.FIT_XY
            QnLottieScaleType.FIT_START -> ImageView.ScaleType.FIT_START
            QnLottieScaleType.FIT_CENTER -> ImageView.ScaleType.FIT_CENTER
            QnLottieScaleType.FIT_END -> ImageView.ScaleType.FIT_END
            QnLottieScaleType.CENTER -> ImageView.ScaleType.CENTER
            QnLottieScaleType.CENTER_CROP -> ImageView.ScaleType.CENTER_CROP
            QnLottieScaleType.CENTER_INSIDE -> ImageView.ScaleType.CENTER_INSIDE
        }
    }

}