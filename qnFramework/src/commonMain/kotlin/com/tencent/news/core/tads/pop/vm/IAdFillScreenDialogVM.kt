package com.tencent.news.core.tads.pop.vm

import com.tencent.news.core.platform.getPlatformDate
import com.tencent.news.core.pop.IComposePopVM
import com.tencent.news.core.tads.fullscreenmask.config.AdFullScreenMaskConfig
import com.tencent.news.core.tads.vm.IAdDebugMsgVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 通用弹窗 VM 接口
 */
interface IAdFillScreenDialogVM : IComposePopVM {

    // 中心内容区域的宽高比
    val ratio: Float get() = 375f / 812f    // 标准设计图比例

    // 广告标签文本，默认"广告"
    val adLabelText: String get() = "广告"

    // 关闭按钮lottieUrl
    val closeLottieUrl: String get() = AdFullScreenMaskConfig.getCloseBtnLottieUrl().urlAndroid

    // 行动按钮图片 URL
    val buttonImageUrl: String

    // 倒计时ms数
    val countdownSeconds: Int get() = 5000

    // 底部提示文本内容
    val bottomText: String get() = "点击上方按钮及素材跳转详情页或第三方应用"

    val clickFrameVM: IAdClickFrameVM? get() = null

    // 动画 VM（可选，用于控制进入和退出动画）
    val animationVM: IAdDialogAnimationVM? get() = null

    val debugVM: IAdDebugMsgVM? get() = null

    /**
     * content 是否渲染在通用组件层（行动按钮、倒计时、可点击区域等）之上。
     *
     * - false（默认）：content 在底层，通用组件在上层。适用于图片/视频蒙层等
     *   content 不需要接收点击的场景。
     * - true：content 在上层，通用组件在底层。适用于互动蒙层等 content 内部
     *   包含可交互控件（如切换按钮），需要获得更高点击优先级的场景。
     */
    val contentAboveCommonComponents: Boolean get() = false

    // 是否跳过倒计时（当值变为 true 时，倒计时动画会立即跳到终点，关闭按钮立即可用）
    val skipCountdown: StateFlow<Boolean>

    // 弹窗事件流（用于外部监听弹窗状态，如控制视频播放）
    val dialogEvent: SharedFlow<AdDialogEvent>

    // 弹窗显示时调用
    fun onDialogShow()

    // 弹窗消失时调用
    fun onDialogDismiss()

    // 灰色蒙版区域点击事件
    fun onGreyAreaClick()

    // 行动按钮点击事件
    fun onButtonClick() {}

    // 倒计时结束回调
    fun onCountdownEnd() {}

    // 关闭按钮点击事件
    fun onCloseClick() {}

    fun bindViewContext(enterViewContext: IFillScreenEnterViewContext)

    fun bindDismissAction(action: (() -> Unit)?)

    fun bindCoroutineScope(scope: CoroutineScope?)
}

/**
 * 弹窗生命周期事件
 */
enum class AdDialogEvent {
    SHOW,       // 弹窗展示
    DISMISS     // 弹窗消失
}

/**
 * 退出动画类型
 */
sealed class ExitAnimationType {
    /**
     * 简单渐出动画
     * 仅透明度变化，300ms
     */
    class Simple : ExitAnimationType()

    /**
     * 回缩中心动画
     */
    class Shrink(val pos: Pair<Float, Float>) : ExitAnimationType()
}

/**
 * 退出动画请求
 *
 * @property type 动画类型
 * @property timestamp 请求时间戳，用于触发状态变化
 */
data class ExitAnimationRequest(
    val type: ExitAnimationType,
    val timestamp: Long = getPlatformDate().getCurTimeMillis()
)

/**
 * 入口视图参数接口
 * 用于判断和计算退出动画类型
 */
interface IFillScreenEnterViewContext {
    /**
     * 获取入口视图中心点坐标（屏幕坐标系）
     */
    fun getEnterCenterPos(): Pair<Float, Float>
}
