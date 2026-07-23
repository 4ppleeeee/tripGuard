package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.video.api.IQnVideoView
import com.tencent.news.core.video.api.alpha.IQnAlphaVideoView

interface IAppViewBridge {
    fun createVideoView(param: VideoCreateParam): IQnVideoView?
    fun createStreamVideoView(param: VideoCreateParam): Any?
    fun createAlphaVideoView(context: IKmmContext?): IQnAlphaVideoView?
}

@OptIn(KmmInternalApi::class)
fun appViewBridge(): IAppViewBridge? = QnFrameworkLogic.viewBridge

data class VideoCreateParam(
    val context: IKmmContext?,
    val scene: Int
)

object QnVideoScene {
    const val AD_MARKET_CARD = 50
    const val GAME_FEEDS_BIG_CARD = 51
    const val EVENT_VIDEO_CARD = 52
    const val AD_FULLSCREEN_VIDEO = 53              // 竖版视频广告全屏页面
    const val AI_SUBSCRIBE_INLINE_VIDEO_CARD = 54   // AI订阅内嵌视频卡片
    const val AD_VIDEO_TEMPLATE_FINISH_CARD = 55    // 竖版视频结束页卡
    const val LONG_VIDEO_DETAIL = 56
    const val AD_MID_ARTICLE = 57                   // 文中视频广告（横版/竖版大图视频化），强制静音、不响应物理音量键
}