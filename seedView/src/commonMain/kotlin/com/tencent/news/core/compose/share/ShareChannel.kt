package com.tencent.news.core.compose.share

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.news.core.app.constants.IIconFont
import com.tencent.news.core.app.constants.QnIconFont
import com.tencent.news.core.compose.scaffold.modifiers.QnViewDtElementIds
import com.tencent.news.core.dt.constants.IDtElementId
import com.tencent.news.core.resources.Res
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.view.setup.ViewServiceBridge

data class ShareChannelViewModel constructor(
    val name: String,
    val channel: IShareChannel,
    val dtEid: IDtElementId,
    val icon: Painter? = null,
    val iconFont: IIconFont? = null,
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
    buildShareChannels(DefaultCardShareChannels)

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
            dtEid = QnViewDtElementIds.ShareFriends
        )


        ShareChannel.WEIXIN_MOMENTS -> ShareChannelViewModel(
            name = "朋友圈",
            icon = Res.drawable.share_wx_moment_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareMoments
        )


        ShareChannel.QQ -> ShareChannelViewModel(
            name = "QQ好友",
            icon = Res.drawable.share_qq_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareQQ
        )


        ShareChannel.QZONE -> ShareChannelViewModel(
            name = "QQ空间",
            icon = Res.drawable.share_qzone_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareQZone
        )

        ShareChannel.WEIBO -> ShareChannelViewModel(
            name = "新浪微博",
            icon = Res.drawable.share_sina_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareSina
        )

        ShareChannel.WORK_WEIXIN -> ShareChannelViewModel(
            name = "企业微信",
            icon = Res.drawable.share_work_weixin_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareWorkWeixin
        )

        ShareChannel.CHANNEL_POST -> ShareChannelViewModel(
            name = "海报分享",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.SharePostCard
        )

        ShareChannel.MORNING_POST -> ShareChannelViewModel(
            name = "海报分享",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.SharePostCard
        )

        ShareChannel.AIQA_POST -> ShareChannelViewModel(
            name = "分享海报",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.SharePostCard
        )

        ShareChannel.TIMELINE_POST -> ShareChannelViewModel(
            name = "分享海报",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.SharePostCard
        )

        ShareChannel.EVENT_POST -> ShareChannelViewModel(
            name = "分享海报",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.SharePostCard
        )

        ShareChannel.COPY_LINK -> ShareChannelViewModel(
            name = "复制链接",
            iconFont = QnIconFont.COPY_LINK,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareCopyLink
        )

        ShareChannel.SCREENSHOT -> ShareChannelViewModel(
            name = "截屏分享",
            iconFont = QnIconFont.SCREENSHOT,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareScreenshot
        )

        ShareChannel.SAVE_IMAGE -> ShareChannelViewModel(
            name = "保存图片",
            iconFont = QnIconFont.DOWNLOAD,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareSaveImage
        )

        ShareChannel.SAVE_VIDEO -> ShareChannelViewModel(
            name = "保存视频",
            iconFont = QnIconFont.DOWNLOAD,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareSaveVideo
        )

        ShareChannel.SYSTEM -> ShareChannelViewModel(
            name = "系统分享",
            iconFont = QnIconFont.SHARE_REGULAR,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.ShareSystem
        )

        ShareChannel.PDF_SHARE -> ShareChannelViewModel(
            name = "生成PDF",
            icon = Res.drawable.share_pdf_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.SharePdf
        )

        ShareChannel.AIGC_POSTER -> ShareChannelViewModel(
            name = "海报分享",
            icon = Res.drawable.share_poster_icon,
            channel = createShareChannel(),
            dtEid = QnViewDtElementIds.SharePostCard
        )
    }
}

private val DefaultCardShareChannels = listOf(
    ShareChannel.SAVE_IMAGE,
    ShareChannel.WEIXIN,
    ShareChannel.WEIXIN_MOMENTS,
    ShareChannel.QQ,
    ShareChannel.QZONE,
    ShareChannel.WORK_WEIXIN,
    ShareChannel.SYSTEM
)

private fun ShareChannel.createShareChannel(): IShareChannel =
    ViewServiceBridge.impl.createShareChannel(this)
