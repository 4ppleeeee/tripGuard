package com.tencent.news.core.tads.feeds.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * 超级蒙层-前置蒙层卡片VM
 *
 * 用于在广告卡片上显示前置蒙层（灰色蒙层 + Lottie动画）
 * 当用户滚动使广告曝光比例达到阈值后，触发展开全屏超级蒙层弹窗
 */
interface IAdMaskViewCardVM : IAdFeedOverlayVM {
    val isVisible: StateFlow<Boolean> // 是否显示前置蒙层（根据超级蒙层类型和频控逻辑决定）

    val backgroundUrl: String // 蒙层背景图片 URL（高斯模糊背景）

    val iconLottieUrl: String // 引导图标 Lottie 动画 URL（循环手势引导）

    val progressLottieUrl: String // 进度条 Lottie 动画 URL（倒计时进度）

    val lottieLoadTime: Int      // 前置蒙层加载时间毫秒

    val superMaskImgUrl: String  // 只允许鸿蒙使用，曲线救国鸿蒙图片预加载

    val isLottieAnimating: StateFlow<Boolean> // Lottie 动画是否正在播放

    val isProgressComplete: StateFlow<Boolean> // 进度 Lottie 动画是否已完成


    val videoAction: SharedFlow<AdMaskVideoAction> // 视频控制指令流

    fun onWillAppear()                  // 触发曝光回调

    fun onProgressAnimationComplete()   // 进度动画完成回调，准备展开全屏蒙层

    fun onClickGreyBg()                 // 上报无效点击（点击灰色区域）

    fun injectScope(scope: CoroutineScope)

    fun clearCoroutineScope()

    fun onLottieDownloadStatusChange(status: String)
}

/**
 * 蒙层触发的视频控制指令
 */
enum class AdMaskVideoAction {
    STOP_ALL,       // 停止所有视频（防止背景音干扰）
    RESUME_AUTO     // 恢复自动播放逻辑（蒙层消失后）
}
