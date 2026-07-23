package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController
import com.tencent.news.core.compose.view.dialog.DialogContent
import com.tencent.news.core.compose.view.dialog.DialogOnDismiss
import com.tencent.news.core.compose.view.dialog.DialogShowType
import com.tencent.news.core.compose.view.dialog.DialogThemeMode
import com.tencent.news.core.compose.view.dialog.IDialog
import com.tencent.news.core.page.vm.IStructDialogVM

@Composable
fun StructAnimatedDialog(
    vm: IStructDialogVM,
    enableDragToDismiss: Boolean = true,
    /**
     * 可选回调：在用户触发关闭（点击遮罩/BackHandler/拖拽）时立即调用，
     * 早于 [IStructDialogVM.dismissDialog]（后者在退出动画结束后才调用）。
     * 可用于在弹窗退出动画期间同步驱动外部动画（如视频还原），消除停顿感。
     */
    onDismissRequest: (() -> Unit)? = null,
    /**
     * 可选回调：拖拽过程中实时通知当前拖拽偏移量（像素）。
     * 可用于在拖拽时同步驱动外部动画（如视频跟手缩放），消除停顿感。
     */
    onDragOffsetChange: ((Float) -> Unit)? = null,
    /**
     * 可选：底部安全区 Spacer 的背景色。
     * 默认为 null，走 [IDialog.safeAreaBackgroundColorProvider] 的默认值（bgBlock）。
     * 弹窗内容背景色与默认值不一致时，传入对应颜色以保持视觉连贯。
     */
    safeAreaBackgroundColor: (@Composable () -> Color)? = null,
    /**
     * 是否由 content 自己处理底部安全区。
     * 默认 false：框架在 content 外部追加安全区 Spacer。
     * 设置为 true 后，框架不再追加外部安全区，content 会从屏幕底部开始布局。
     */
    contentHandlesBottomSafeArea: Boolean = false,
    /**
     * 可选：自定义弹窗背景内容（替换默认半透明遮罩）。
     * 传入后会替换默认的 shadow25 背景遮罩；如需"完全透明无暗色"，传入一个空内容的 Composable 即可。
     */
    customBackgroundContent: (@Composable () -> Unit)? = null,
    dialogShowType: DialogShowType = DialogShowType.BottomSheet,
    content: @Composable () -> Unit
) {
    val isVisible by vm.showDialogState.collectAsState()

    val dialogController = LocalDialogController.current

    // isLandscape 为 true 时强制暗色主题，其他情况跟随系统
    val themeMode = if (vm.isLandscape) DialogThemeMode.DARK else DialogThemeMode.AUTO

    class WrapperDialog : IDialog() {
        override val showType: DialogShowType = dialogShowType

        override val content: DialogContent = { _, _ ->
            content()
        }

        override val themeMode: DialogThemeMode = themeMode

        override val onDismiss: DialogOnDismiss = {
            vm.dismissDialog()
        }

        override val enableDragToDismiss: Boolean = enableDragToDismiss

        override val onDismissStart: DialogOnDismiss? = onDismissRequest

        override val onDragOffsetChange: ((Float) -> Unit)? = onDragOffsetChange

        override val safeAreaBackgroundColorProvider: (@Composable () -> Color)? =
            safeAreaBackgroundColor

        override val contentHandlesBottomSafeArea: Boolean = contentHandlesBottomSafeArea

        override val customBackgroundContent: (@Composable () -> Unit)? = customBackgroundContent

        // 拦截所有穿透事件（包括滑动手势），防止手势传递到底层 VerticalPager 导致视频切换
        override val forceProtectClickEvent: Boolean = true

    }

    // 用 remember 持有同一个 WrapperDialog 实例，避免外层 StructAnimatedDialog 因任意状态读
    // （如 vm.showDialogState）重组时反复 new WrapperDialog 并 emit 到 dialogController，
    // 导致 outer CollectDialogState 误以为换了一个 dialog，把旧 dialog 整棵子树（含 content）
    // 从 composition 中移除（典型表现：评论面板内的输入弹窗一闪即消失、长按半屏登录后内容被卸载）。
    val dialog = remember { mutableStateOf<WrapperDialog?>(null) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            val current = dialog.value
            if (current == null) {
                val targetDialog = WrapperDialog()
                dialog.value = targetDialog
                dialogController.showDialog(targetDialog)
            }
        } else {
            val targetDialog = dialog.value
            if (targetDialog != null) {
                dialogController.dismissDialog(targetDialog)
                dialog.value = null
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val targetDialog = dialog.value
            if (targetDialog != null) {
                dialogController.dismissDialog(targetDialog)
                dialog.value = null
            }
        }
    }
}