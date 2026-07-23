package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.share.api.IKmmShareDataOption
import com.tencent.news.core.share.api.IShareReportData
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.share.api.ShareSceneType
import com.tencent.news.core.tads.model.IKmmAdOrder
import com.tencent.news.core.vm.IAiShareMetadataRepoStub
import com.tencent.news.core.vm.IDetailModelStub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * 分享相关服务
 *
 * 备注：临时方案，后续将分享整个下沉到kmm
 */
interface IAppShare {

    @Deprecated("Use DialogController instead")
    suspend fun showSharePage(context: IKmmContext?, shareData: IKmmShareData)

    fun share2Desktop(context: IKmmContext?, info: ShortCutInfo?)
}

data class ShareData(
    override val item: IKmmFeedsItem? = null,
    override val channelId: String = "",
    override val sourceNewsDetail: IDetailModelStub? = null,
    override val originAd: IKmmAdOrder? = null,
    override val option: IKmmShareDataOption? = null,
    override val items: List<IKmmFeedsItem>? = null,
) : IKmmShareData

data class ReportData(
    override val hostUrl: String,
    override val params: Map<String, String?> = emptyMap(),
    override val requireLogin: Boolean = false,
) : IShareReportData

@Serializable
data class ShortCutInfo(
    val safariUrl: String? = null, // iOS专用
    val id: String = "",
    val desc: String = "",
    val icon: String = "",
    val name: String = "",
    val scheme: String = "",
) : IKmmKeep

class KmmShareDataOption(
    val item: IKmmFeedsItem?,
    val showPreviewPoster: Boolean,
    override val sharePos: String = "",
    override val onlyFirstLine: Boolean = false,
    override val shareScene: ShareSceneType = ShareSceneType.DEFAULT,
    override val aiMetadata: IAiShareMetadataRepoStub? = null,
    override val posterShareChannel: ShareChannel? = null,
    override val videoUrl: String = "",
    override val arkData: String = "",
    override val shareContent: String = "",
) : IKmmShareDataOption {
    override val imageWeiXinQqUrls: Array<String?> = arrayOf(item?.shareDto?.shareImg)
    override val imageWeiBoQZoneUrls: Array<String?> = arrayOf(item?.shareDto?.shareImg)
    override var useShareInfoFirst: Boolean = false
    override val isUnSafeComment: Boolean = false
    override val showPosterPreview: Boolean = showPreviewPoster
}

@OptIn(KmmInternalApi::class)
fun appShare(): IAppShare = AppShare(QnFrameworkLogic.appShare)

private class AppShare(private val platformShare: IAppShare?) : IAppShare {
    @Deprecated("Use DialogController instead")
    override suspend fun showSharePage(context: IKmmContext?, shareData: IKmmShareData) {
        withContext(Dispatchers.Main) {
            platformShare?.showSharePage(context, shareData)
        }
    }

    override fun share2Desktop(context: IKmmContext?, info: ShortCutInfo?) {
        platformShare?.share2Desktop(context, info)
    }
}
