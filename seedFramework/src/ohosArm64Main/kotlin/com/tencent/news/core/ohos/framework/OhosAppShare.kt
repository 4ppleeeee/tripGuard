package com.tencent.news.core.ohos.framework

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.platform.api.IAppShare
import com.tencent.news.core.platform.api.ShortCutInfo
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppShare = JSValue

/**
 * 注册 ohos 端 IAppShare 实现，使得 kmm 侧 `appShare()` 能够路由到 ArkTS 层的微信 / 企业微信分享。
 */
@OptIn(KmmInternalApi::class)
fun setupOhosAppShare(share: IOhosAppShare) {
    val nativeShare = share.asOhosAppShare()
    QnFrameworkLogic.appShare = OhosAppShareProvider(nativeShare)
}

private class OhosAppShareProvider(
    private val share: OhosAppShare
) : IAppShare {

    override suspend fun showSharePage(context: IKmmContext?, shareData: IKmmShareData) = Unit

    override suspend fun shareToChannel(
        context: IKmmContext?,
        shareData: IKmmShareData,
        channelId: String,
    ) {
        val normalizedChannelId = channelId.ifBlank { shareData.channelId.orEmpty() }
        val item = shareData.item?.flexDto
        share.shareToChannel(
            channelId = normalizedChannelId,
            title = item?.title.orEmpty(),
            desc = shareData.option?.shareContent.orEmpty(),
            url = item?.url.orEmpty(),
            imagePath = shareData.imagePathForChannel(normalizedChannelId),
        )
    }

    override fun isShareChannelSupported(channelId: String): Boolean {
        return share.isShareChannelSupported(channelId)
    }

    override fun share2Desktop(context: IKmmContext?, info: ShortCutInfo?) {
        // no-op: 鸿蒙端桌面快捷方式未接入 IAppShare。
    }

    override fun shareLogToWeChat(onResult: (Boolean) -> Unit) {
        share.shareLogToWeChat()
        onResult(true)
    }

    override fun shareLogToWeCom(onResult: (Boolean) -> Unit) {
        share.shareLogToWeCom()
        onResult(true)
    }
}

@KNCallback
interface OhosAppShare {
    fun isShareChannelSupported(channelId: String): Boolean

    fun shareLogToWeChat()

    fun shareLogToWeCom()

    fun shareToChannel(
        channelId: String,
        title: String,
        desc: String,
        url: String,
        imagePath: String,
    )
}

private fun IKmmShareData.imagePathForChannel(channelId: String): String {
    val normalizedChannelId = channelId.uppercase()
    val urls = when (normalizedChannelId) {
        "QZONE",
        "WEIBO" -> option?.imageWeiBoQZoneUrls
        else -> option?.imageWeiXinQqUrls
    }
    return urls?.firstOrNull { !it.isNullOrBlank() }.orEmpty()
}
