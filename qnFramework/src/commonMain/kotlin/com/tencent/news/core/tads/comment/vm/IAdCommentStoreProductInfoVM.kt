package com.tencent.news.core.tads.comment.vm

import com.tencent.news.core.compose.platform.IComposeDemoPage
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tads.model.QnAdOrder
import com.tencent.news.core.tads.vm.IAdActionBtnVM
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Suppress("ModelClassRule")
@Serializable
data class AdCommentStoreProductInfoPageArgs(
    val adOrder: QnAdOrder, // 同时兼容jsonStr和obj格式解析
) : IComposePageArgs, IComposeDemoPage, IKmmKeep {
    override var runInDemo: Boolean = false
}

/** 商品图加载失败时使用的兜底资源语义。 */
enum class AdCommentStoreProductImageFallbackResource {
    WECHAT_STORE_PRODUCT_ICON,
    LIVE_SHOP_PRODUCT_ICON
}

interface IAdCommentStoreProductLabelVM : IKmmKeep {
    val isGuarantee: Boolean
    val content: String
    val stateContent: StateFlow<String>?
    val data: List<String>
    val isLabelFont: Boolean
    val isSales: Boolean
    val showPayScoreIcon: Boolean
    val payScoreIconTags: List<String>
}

interface IAdCommentStoreProductInfoVM : IKmmKeep {
    val commentProductInfoHeight: Int
    val actionButton: IAdActionBtnVM
    val actionText: String
    val productTitle: String
    val productImageUrl: String
    val productImageFallbackResource: AdCommentStoreProductImageFallbackResource
    val liveStatusImageUrl: String?
    val displayLabels: List<IAdCommentStoreProductLabelVM>
    val fallbackLabelText: String?
    val labelLoopTime: Long

    /** 处理商品信息条行动按钮点击。 */
    fun onActionClick()

    /** 处理商品信息条负反馈入口点击。 */
    fun onUnlikeClick()

    /** 更新评论区商品信息条的宿主回调；行动按钮点击由 actionButton 在 KMM 内处理。 */
    fun updateCommentProductInfoHostCallback(
        unlikeHandler: (() -> Unit)?
    )
}
