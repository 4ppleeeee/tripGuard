@file:Suppress("PropertyName", "VariableNaming")

package com.tencent.news.core.page.model

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.KmmShareInfo
import com.tencent.news.core.list.model.QnKmmHotEvent
import com.tencent.news.core.service.FrameworkServiceBridge
import com.tencent.news.core.share.api.BizShareChannels
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.tag.model.IKmmTagInfo
import com.tencent.news.core.tag.model.QnTagInfo
import com.tencent.news.core.user.model.IUserInfo
import com.tencent.news.core.user.model.QnUserInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


annotation class ShareBtnShowType {

    companion object {

        // 带灰色背景框的样式 https://universal-1258344701.shiply-cdn.qq.com/config_template/183/1712494325273/rc-upload-1712494310475-3.png
        const val CORNER_BG_STYLE = 2
    }

}

sealed interface ShareOperationType {
    data class Favorite(
        val feedsItem: IKmmFeedsItem? = null,
        val tagInfo: IKmmTagInfo? = null,
        val favoriteSource: FavoriteSource
    ) : ShareOperationType

    data object NightMode : ShareOperationType

    data object Font : ShareOperationType

    data class Gift(
        val tagInfo: IKmmTagInfo? = null,
        val cardInfo: IUserInfo? = null,
        val isColumnPay: Boolean = false,
    ) : ShareOperationType
}

enum class FavoriteSource {
    LIST,  // 使用ListIteKmmFeedsItem KmmFeedsItem构造
    TAG,   // 使用TagItem构造
}

@Serializable
@SerialName(StructWidgetType.SHARE_BTN)
open class ShareBtnWidget(
    @Serializable(ShareBtnWidgetDataWrapperSerializer::class)
    override var data: ShareBtnWidgetData? = null,

    var uiConfig: ShareBtnWidgetUI = ShareBtnWidgetUI(),

    @Transient
    var shareChannel: List<ShareChannel> = BizShareChannels.eventPost,

    @Transient
    var operationButtons: List<ShareOperationType>? = null,  // 分享弹窗第二行操作按钮，不需要时留空即可
) : StructBtnWidget<ShareBtnWidgetData>(), IKmmKeep {

    override val asWidgetVM: IShareBtnWidgetViewModel by lazy {
        FrameworkServiceBridge.impl.createShareBtnVM(this)
    }

    override fun getWidgetType() = StructWidgetType.SHARE_BTN

    companion object {
        fun build(
            shareData: KmmShareInfo,
            shareChannel: List<ShareChannel> = BizShareChannels.eventPost,
            iconFont: IconFont? = null
        ): ShareBtnWidget {
            return ShareBtnWidget().apply {
                this.shareChannel = shareChannel
                this.data = ShareBtnWidgetData().apply {
                    this.shareData = shareData
                    this.iconFont = iconFont
                }
            }
        }
    }

}

@Serializable
class ShareBtnWidgetUI : IKmmKeep {
    var isBarIconDark: Boolean = false      // true：TitleBar图标默认用黑色图标（页面header为浅色、图标为黑色）
}

@Serializable
open class ShareBtnWidgetData : StructBtnWidgetData(), IKmmKeep {

    internal var share_data: KmmShareInfo? = null
    var shareData: KmmShareInfo?
        get() = share_data
        set(value) {
            share_data = value
        }

    var hot_event: QnKmmHotEvent? = null

    @SerialName("user_info")
    var userInfo: QnUserInfo? = null

    @SerialName("tag_info_item")
    var tagInfo: QnTagInfo? = null

    var reportEvent: String? = null

    @Transient
    val reportParams = mutableMapOf<String, Any>()

    companion object {

        fun createShareDataForTagInfo(tagInfo: IKmmTagInfo?, shareCount: Long): KmmShareInfo? {
            tagInfo ?: return null

            return KmmShareInfo().apply {
                share_title = tagInfo.homePageInfo?.shareTitle ?: ""
                share_desc = tagInfo.homePageInfo?.shareAbstract ?: ""
                share_content = tagInfo.homePageInfo?.shareAbstract ?: ""
                share_img = tagInfo.homePageInfo?.sharePic ?: ""
                share_url = tagInfo.homePageInfo?.shareUrl ?: ""
                enable_share = tagInfo.homePageInfo?.openShare ?: true
                this.shareCount = shareCount
            }
        }

    }

}

class ShareBtnWidgetDataWrapperSerializer : DataWrapperSerializer<ShareBtnWidgetData>(
    StructWidgetType.SHARE_BTN, ShareBtnWidgetData.serializer()
)

interface IShareBtnWidgetViewModel : StructWidgetViewModel {

    val enableShare: StateFlow<Boolean>
    val iconFont: IconFont?
    val isBottomStyle: Boolean
    val shareChannels: List<ShareChannel>
    val showBtnBg: Boolean
    val shareInNative: Boolean

    val shareCount: MutableSharedFlow<Long>

    val operationButtons: List<ShareOperationType>?

    fun getShareData(): IKmmShareData
    fun getShareDesc(count: Long): String

    suspend fun onShareSuccess()

}