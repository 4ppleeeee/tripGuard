package com.tencent.news.core.compose.platform

import com.tencent.news.core.channel.model.QnKmmChannelInfo
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IKmmFeedsItem
import kotlinx.serialization.Serializable

@Serializable
@Suppress("ModelClassRule", "RedundantConstructorKeyword")
data class AudioPodChannelArgs constructor(
    val channelInfo: QnKmmChannelInfo,
    val scene: String = "CHANNEL",
    val nativeDialogAnchorOffsetY: Float = 0f
) : IKmmKeep, IComposePageArgs {
    companion object {
        const val CHANNEL = "CHANNEL"
        const val STANDALONE = "STANDALONE"
    }
}


object AudioPodChannelPageRefCounter {

    private var pageRefCount: Int = 0
    private var onAllPagesReleased: (() -> Unit)? = null
    private var onRecommendCardRefreshed: ((List<IKmmFeedsItem>) -> Unit)? = null

    /** 所有页面释放的回调，用于释放全局缓存变量 */
    fun registerOnAllPagesReleased(callback: (() -> Unit)?) {
        onAllPagesReleased = callback
    }

    /** 今日推荐卡片缓存更新的回调 */
    fun registerOnRecommendCardRefreshed(callback: ((List<IKmmFeedsItem>) -> Unit)?) {
        onRecommendCardRefreshed = callback
    }

    /** 通知今日推荐卡片缓存更新 */
    fun notifyRecommendCardRefreshed(newsList: List<IKmmFeedsItem>) {
        onRecommendCardRefreshed?.invoke(newsList)
    }

    fun get() {
        pageRefCount++
    }

    fun release() {
        if (pageRefCount <= 0) {
            return
        }
        pageRefCount--
        if (pageRefCount <= 0) {
            onAllPagesReleased?.invoke()
        }
    }
}
