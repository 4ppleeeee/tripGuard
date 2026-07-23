package com.tencent.news.core.tads.pop.vm

import kotlinx.coroutines.flow.StateFlow

/**
 * 广告全屏弹窗动画 VM 接口
 * 
 * 专门管理广告弹窗的进入和退出动画相关状态
 */
interface IAdDialogAnimationVM {
    
    /**
     * 退出动画请求（Dialog 监听此状态）
     * 包含动画类型信息，实现类可以根据关闭原因设置不同的动画类型
     */
    val exitAnimationRequest: StateFlow<ExitAnimationRequest?>
    
    /**
     * 退出动画播放完成回调
     * Dialog 调用，通知 VM 可以真正关闭弹窗了
     */
    fun onExitAnimationFinished()
}
