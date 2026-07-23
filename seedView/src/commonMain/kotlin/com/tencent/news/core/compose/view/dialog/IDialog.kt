package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.pop.IPopType
import kotlinx.coroutines.CoroutineScope

typealias DialogContent = @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit

typealias DialogOnDismiss = () -> Unit

abstract class IDialog {

    var isNative: Boolean = false

    abstract val showType: DialogShowType

    abstract val content: DialogContent

    open val safeAreaBackgroundColorProvider: (@Composable () -> Color)? = null

    /**
     * BottomSheet 是否由 content 自己处理底部安全区。
     *
     * 默认 false：框架在 content 外部追加安全区 Spacer，保持历史行为。
     * 设置为 true 后，框架不再追加外部安全区，content 会从屏幕底部开始布局。
     */
    open val contentHandlesBottomSafeArea: Boolean = false

    open val onDismiss: DialogOnDismiss? = null

    /**
     * 可选回调：在退出动画开始的瞬间立即触发（早于 [onDismiss]，后者在动画结束后才调用）。
     * 可用于在弹窗退出动画期间同步驱动外部动画（如视频区域还原），消除停顿感。
     */
    open val onDismissStart: DialogOnDismiss? = null

    /**
     * 自定义背景内容（可选）
     * 如果提供，将替换默认的半透明遮罩背景
     */
    open val customBackgroundContent: (@Composable () -> Unit)? = null

    /**
     * fix:是否强制拦截点击事件，出现点击穿透的时候改为true
     */
    open val forceProtectClickEvent: Boolean = false

    /**
     * fix:是否强制拦截背景事件，默认为true，为true可能会有吃掉弹窗内事件
     */
    open val forceProtectTouchEvent: Boolean = true

    /**
     * 二次分享时是否自动关闭当前弹窗
     * true: 点击海报分享等二次分享渠道后自动关闭当前弹窗（默认行为，适用于早晚报等场景）
     * false: 点击二次分享渠道后不关闭当前弹窗（适用于频道分享等场景）
     */
    open var autoDismissOnTwiceClick: Boolean = true

    /**
     * 是否启用下拉拖拽关闭（仅对 BottomSheet 类型生效）
     * 启用后用户可以通过向下拖拽来关闭弹窗，拖拽过程中内容跟手移动。
     * 当弹窗内容包含可滑动列表时，列表滚动到顶部后继续下拉才会触发拖拽关闭。
     */
    open val enableDragToDismiss: Boolean = true

    /**
     * 拖拽关闭的配置参数（仅在 [enableDragToDismiss] 为 true 时生效）
     */
    open val dragToDismissConfig: DragToDismissConfig = DragToDismissConfig()

    /**
     * 可选回调：拖拽过程中实时通知当前拖拽偏移量（像素）。
     * 可用于在拖拽时同步驱动外部动画（如视频跟手缩放），消除停顿感。
     * 回弹/取消时会以 0f 回调，表示偏移归零。
     */
    open val onDragOffsetChange: ((Float) -> Unit)? = null

    /**
     * 弹窗主题模式
     * - [DialogThemeMode.AUTO]: 跟随系统日夜间模式（默认）
     * - [DialogThemeMode.DARK]: 强制暗色主题
     * - [DialogThemeMode.LIGHT]: 强制亮色主题
     */
    open val themeMode: DialogThemeMode = DialogThemeMode.AUTO

    var customPopType: IPopType? = null

}
