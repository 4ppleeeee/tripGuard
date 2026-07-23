package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.adaptive.AdaptiveDialog
import com.tencent.news.core.pop.PopType
import kotlinx.coroutines.CoroutineScope

typealias DialogContent = @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit

typealias DialogOnDismiss = () -> Unit

abstract class IDialog {

    var isNative: Boolean = false

    abstract val showType: DialogShowType

    open val displayType: AdaptiveDialog.DisplayType? = null

    abstract val content: DialogContent

    open val safeAreaBackgroundColorProvider: (@Composable () -> Color)? = null

    open val onDismiss: DialogOnDismiss? = null

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
     * 是否允许点击弹窗外的半透明遮罩区域来关闭弹窗。
     * true：点击遮罩关闭（默认，兼容既有弹窗行为）
     * false：点击遮罩不关闭，仅支持由弹窗内容自身（关闭按钮）或返回键关闭
     *
     * 适用场景：选集弹窗、专辑弹窗等需要用户明确点击关闭按钮的场景
     */
    open val dismissOnBackdropClick: Boolean = true

    /**
     * 二次分享时是否自动关闭当前弹窗
     * true: 点击海报分享等二次分享渠道后自动关闭当前弹窗（默认行为，适用于早晚报等场景）
     * false: 点击二次分享渠道后不关闭当前弹窗（适用于频道分享等场景）
     */
    open var autoDismissOnTwiceClick: Boolean = true

    var customPopType: PopType? = null

}



