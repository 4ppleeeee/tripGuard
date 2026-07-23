package com.tencent.news.core.compose.share

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.resources.Res
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.BizShareChannels
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.view.setup.ViewServiceBridge

data class ShareChannelViewModel constructor(
    val name: String,
    val channel: IShareChannel,
    val dtEid: DtElementId,
    val icon: Painter? = null,
    val iconFont: IconFont? = null,
)

class ShareViewModel {
    val channels = mutableListOf<ShareChannelViewModel>()
    var postPreviewData: PostPreviewData? = null

    internal fun addChannels(vararg channelViewModels: ShareChannelViewModel) {
        channelViewModels.forEach {
            if (!it.channel.isSupported()) {
                return@forEach
            }
            channels.add(it)
        }
    }

    internal fun addChannel(channelViewModel: ShareChannelViewModel) {
        if (!channelViewModel.channel.isSupported()) {
            return
        }
        channels.add(channelViewModel)
    }

    internal fun setPostPreviewData(data: PostPreviewData) {
        postPreviewData = data
    }
}

@Composable
fun buildCardShareChannels(): ShareViewModel =
    buildShareChannels(BizShareChannels.postShare)

@Composable
fun buildShareChannels(channels: List<ShareChannel>): ShareViewModel {
    return ShareViewModel().apply {
        addChannels(*channels.map { it.findViewModel() }.toTypedArray())
    }
}

@Composable
private fun ShareChannel.findViewModel(): ShareChannelViewModel {
    return when (this) {
        ShareChannel.WEIXIN -> ShareChannelViewModel(
            name = "微信好友",
            icon = Res.drawable.share_weixin_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareFriends
        )


        ShareChannel.WEIXIN_MOMENTS -> ShareChannelViewModel(
            name = "朋友圈",
            icon = Res.drawable.share_wx_moment_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareMoments
        )


        ShareChannel.QQ -> ShareChannelViewModel(
            name = "QQ好友",
            icon = Res.drawable.share_qq_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareQQ
        )


        ShareChannel.QZONE -> ShareChannelViewModel(
            name = "QQ空间",
            icon = Res.drawable.share_qzone_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareQZone
        )

        ShareChannel.WEIBO -> ShareChannelViewModel(
            name = "新浪微博",
            icon = Res.drawable.share_sina_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareSina
        )

        ShareChannel.WORK_WEIXIN -> ShareChannelViewModel(
            name = "企业微信",
            icon = Res.drawable.share_work_weixin_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareWorkWeixin
        )

        ShareChannel.CHANNEL_POST -> ShareChannelViewModel(
            name = "海报分享",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.SharePostCard
        )

        ShareChannel.MORNING_POST -> ShareChannelViewModel(
            name = "海报分享",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.SharePostCard
        )

        ShareChannel.AIQA_POST -> ShareChannelViewModel(
            name = "分享海报",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.SharePostCard
        )

        ShareChannel.TIMELINE_POST -> ShareChannelViewModel(
            name = "分享海报",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.SharePostCard
        )

        ShareChannel.EVENT_POST -> ShareChannelViewModel(
            name = "分享海报",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.SharePostCard
        )

        ShareChannel.COPY_LINK -> ShareChannelViewModel(
            name = "复制链接",
            iconFont = IconFont.COPY_LINK,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareCopyLink
        )

        ShareChannel.SCREENSHOT -> ShareChannelViewModel(
            name = "截屏分享",
            iconFont = IconFont.SCREENSHOT,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareScreenshot
        )

        ShareChannel.SAVE_IMAGE -> ShareChannelViewModel(
            name = "保存图片",
            iconFont = IconFont.DOWNLOAD,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareSaveImage
        )

        ShareChannel.SAVE_VIDEO -> ShareChannelViewModel(
            name = "保存视频",
            iconFont = IconFont.DOWNLOAD,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareSaveVideo
        )

        ShareChannel.SYSTEM -> ShareChannelViewModel(
            name = "系统分享",
            iconFont = IconFont.SHARE_REGULAR,
            channel = createShareChannel(),
            dtEid = DtElementId.ShareSystem
        )

        ShareChannel.PDF_SHARE -> ShareChannelViewModel(
            name = "生成PDF",
            icon = Res.drawable.share_pdf_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.SharePdf
        )

        ShareChannel.AIGC_POSTER -> ShareChannelViewModel(
            name = "海报分享",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = DtElementId.SharePostCard
        )
    }
}

private fun ShareChannel.createShareChannel(): IShareChannel =
    ViewServiceBridge.impl.createShareChannel(this)
