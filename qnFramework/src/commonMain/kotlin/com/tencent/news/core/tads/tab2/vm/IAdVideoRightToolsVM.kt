package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.tads.vm.IAdvertiserVM
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** 广告竖版视频右侧工具栏一次性 UI 事件。 */
sealed class AdVideoRightToolsUiEvent {
    /** 请求 UI 展示分享弹窗，弹窗数据由 VM 实现层完成业务组装。 */
    data class ShowShareDialog(
        val channels: List<ShareChannel>,
        val shareData: IKmmShareData,
        val isNative: Boolean = true,
    ) : AdVideoRightToolsUiEvent()
}

interface IAdVideoRightToolsVM {

    // 广告主头像：
    val advertiser: IAdvertiserVM

    // 点赞按钮：
    val likeCount: StateFlow<Long>
    val isLiked: StateFlow<Boolean>

    // 分享按钮：
    val isShareDialogShow: Boolean

    /** 右侧工具栏一次性 UI 事件，例如请求展示分享弹窗。 */
    val uiEvent: SharedFlow<AdVideoRightToolsUiEvent>

    /** 处理点赞按钮点击。 */
    fun onLikeClick()

    /** 处理分享按钮点击，上报点击并通知 UI 展示分享面板。 */
    fun onShareClick()

    /** 处理分享弹窗关闭结果，由 VM 决定后续业务动作。 */
    fun onShareDialogDismissed(isSuccess: Boolean)

}
