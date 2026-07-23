package com.tencent.kmm.demo.setup

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.tencent.news.core.compose.AndroidLottieView
import com.tencent.news.core.compose.view.LottieDownloadStatus
import com.tencent.news.core.compose.view.ProgressRange
import com.tencent.news.core.compose.view.QnLottieScaleType
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.platform.qnLogcat
import com.tencent.news.core.view.extension.DpEx.dpToPx
import com.tencent.news.core.view.extension.setRoundCornerInPx
import com.tencent.news.lottie.interfaces.OnUrlLottieLoadCallback
import com.tencent.news.lottie.views.NewsLottieView

class WsAndroidLottieView(context: Context) : AndroidLottieView(context = context) {

    private val lottieView: NewsLottieView = NewsLottieView(context)

    override fun setLottieName(name: String, status: String) {
        qnLogcat()?.logD("readCompleteLottie", "setLottieName $name")
        if (name.startsWith("http")) {
            lottieView.setAnimationFromUrl(name, status)
        } else {
            lottieView.setZipFromAssets(context, name, status)
        }
    }

    override fun cancelLastAnimation() {
        lottieView.cancelAnimation()
    }

    override fun setAutoPlay(autoPlay: Boolean) {
        if (autoPlay) {
            lottieView.playAnimation()
            qnLogcat()?.logD("readCompleteLottie", "lottieView.playAnimation()")
        }
    }

    override fun setAspectRatio(aspectRatio: Float) {

    }

    override fun setInfinity(infinity: Boolean) {
        lottieView.loop(infinity)
    }

    override fun applyTheme(isDark: Boolean) {
        lottieView.applyTheme()
    }

    override fun setProgress(progress: Float) {
        if (!progress.isNaN()) {
            lottieView.progress = progress
        }
    }

    override fun onAttachedToWindow(parent: ViewGroup) {
        this.addView(
            lottieView, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
            )
        )
    }

    override fun onDetachedFromWindow(parent: ViewGroup) {
        this.removeView(lottieView)
    }

    override fun setColorFilter(color: String) {
        if (color.isNotNullOrEmpty()) {
            val filter = PorterDuffColorFilter(color.toColorInt(), PorterDuff.Mode.SRC_ATOP)
            lottieView.addColorFilter(filter)
        }
    }

    override fun setScaleType(scaleType: QnLottieScaleType) {
        lottieView.scaleType = scaleType.mapToImageScaleType()
    }

    override fun setScale(scale: Float) {
        lottieView.scale = scale
    }

    override fun setCorner(cornerInDp: Float) {
        // 圆角切在容器view身上
        this.setRoundCornerInPx(cornerInDp.dpToPx())
    }

    override fun setProgressRange(range: ProgressRange) {
        lottieView.setPlayRange(range.startProgress, range.endProgress)
    }

    override fun setTextDelegate(textDelegate: Map<String, String>?) {
        textDelegate?.forEach { entry ->
            lottieView.setTextDelegate(entry.key, entry.value)
        }
    }

    override fun setDownloadCallback(listener: ((String) -> Unit)?) {
        lottieView.addOnUrlLottieLoadCallback(object : OnUrlLottieLoadCallback {
            override fun onLoadSuccess(url: String) {
                lottieView.removeOnUrlLottieLoadCallback(this)
                listener?.invoke(LottieDownloadStatus.toJsonString(LottieDownloadStatus.COMPLETED))
            }

            override fun onLoadFailed(url: String, msg: String?) {
                lottieView.removeOnUrlLottieLoadCallback(this)
                listener?.invoke(LottieDownloadStatus.toJsonString(LottieDownloadStatus.FAILED))
            }
        })
    }

}
