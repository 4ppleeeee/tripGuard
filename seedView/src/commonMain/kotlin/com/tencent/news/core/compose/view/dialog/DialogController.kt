package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.compose.scaffold.theme.LocalColorScheme
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.platform.api.appPopBridge
import com.tencent.news.core.platform.api.appRouter
import com.tencent.news.core.pop.IPopType
import com.tencent.news.core.router.contants.FrameworkViewKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DialogController {

    private val dialogFlow = MutableStateFlow<IDialog?>(null)
    private val overlayDialogFlow = MutableStateFlow<IDialog?>(null)
    private val dialogOrderFlow = MutableStateFlow(0L)
    private val overlayDialogOrderFlow = MutableStateFlow(0L)
    private var nextDialogOrder = 0L

    val isDialogShowing = dialogFlow.map { it != null }

    // 标记当前 controller 下新创建的 dialog 是否默认使用 Native 桥接弹窗
    // 使用时：dialog.apply { isNative = dialogController.isNative }
    var isNative = false

    /**
     * 显示弹窗
     * @param dialog 弹窗组件
     */
    suspend fun showDialog(dialog: IDialog) {
        val order = ++nextDialogOrder
        if (dialog.showType.isOverlay()) {
            overlayDialogOrderFlow.emit(order)
            overlayDialogFlow.emit(dialog)
        } else {
            dialogOrderFlow.emit(order)
            dialogFlow.emit(dialog)
        }
    }

    @Composable
    fun QuickShowDialog(dialog: IDialog) {
        rememberCoroutineScope().launch {
            showDialog(dialog)
        }
    }

    /**
     * 显示弹窗
     * @param scope 当前组合函数作用域
     * @param dialog 弹窗组件
     */
    fun showDialog(scope: CoroutineScope, dialog: IDialog) {
        scope.launch {
            showDialog(dialog)
        }
    }

    /**
     * 显示弹窗
     * @param scope 当前组合函数作用域
     * @param showType 弹窗类型
     * @param content 弹窗内容
     */
    fun showDialog(
        scope: CoroutineScope,
        showType: DialogShowType,
        contentHandlesSafeArea: Boolean = false,
        enableDragToDismiss: Boolean = true,
        content: @Composable () -> Unit
    ) {
        scope.launch {
            showDialog(object : IDialog() {
                override val showType: DialogShowType = showType
                override val enableDragToDismiss: Boolean = enableDragToDismiss
                override val contentHandlesBottomSafeArea: Boolean = contentHandlesSafeArea
                override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit
                    get() = { _, _ -> content() }
            })
        }
    }

    fun hasDialogShowing() = dialogFlow.value != null || overlayDialogFlow.value != null

    /**
     * 关闭Dialog
     * @param dialog 弹窗组件，如果为空则关闭正在显示的弹窗
     */
    fun dismissDialog(dialog: IDialog?) {
        if (dialog != null && dialog == overlayDialogFlow.value) {
            dialog.onDismiss?.invoke()
            overlayDialogFlow.value = null
            return
        }
        if (dialog != null && dialog == dialogFlow.value) {
            dialog.onDismiss?.invoke()
            dialogFlow.value = null
            return
        }
        if (dialog == null) {
            if (dialogFlow.value != null) {
                dialogFlow.value?.onDismiss?.invoke()
                dialogFlow.value = null
            } else {
                overlayDialogFlow.value?.onDismiss?.invoke()
                overlayDialogFlow.value = null
            }
        }
    }

    /**
     * 监听Dialog组件状态并展示
     */
    @Composable
    fun CollectDialogState(glistener: ((visible: Boolean) -> Unit)? = null) {
        val scope = rememberCoroutineScope()
        val dialogState: State<IDialog?> = dialogFlow.collectAsState(null)
        val dialog by remember(this) { dialogState }
        val overlayDialogState: State<IDialog?> = overlayDialogFlow.collectAsState(null)
        val overlayDialog by remember(this) { overlayDialogState }
        val dialogOrder by dialogOrderFlow.collectAsState(0L)
        val overlayDialogOrder by overlayDialogOrderFlow.collectAsState(0L)

        if (overlayDialogOrder > dialogOrder) {
            RenderDialog(dialog, scope)
            RenderOverlayDialog(overlayDialog, scope)
        } else {
            RenderOverlayDialog(overlayDialog, scope)
            RenderDialog(dialog, scope)
        }

        // 分发dialog状态
        val isVisible = dialog != null || overlayDialog != null
        glistener?.invoke(isVisible)
        // 处理compose弹窗和类MiniBar组件的冲突（弹窗时隐藏minibar,弹窗消失时显示minibar）
        appPopBridge()?.onPopVisibilityChanged(isVisible)

        // 关闭所有Dialog
        DisposableEffect(this) {
            onDispose {
                overlayDialogFlow.value = null
                dismissDialog(null)
            }
        }
    }

    @Composable
    private fun RenderDialog(dialog: IDialog?, scope: CoroutineScope) {
        val targetDialog = dialog
        if (targetDialog != null && targetDialog.isNative.isTrue()) {
            // 优先从 customPopType 获取 popType
            //  FIXME: 线上固定传入 PRAISE_DIALOG 类型不合理，需要修复
            val popType: IPopType? = targetDialog.customPopType
            if (popType != null) {
                // 桥接到native弹窗
                scope.launch {
                    dialogFlow.value = null
                    val args = NativeDialogHandlerController.put(targetDialog)
                    appRouter().toComposeDialog(
                        LocalKmmContext,
                        popType,
                        FrameworkViewKey.Dialog.NATIVE_BRIDGE,
                        args
                    )
                }
            }
        } else {
            when (dialog?.showType) {
                DialogShowType.BottomSheet -> BottomSheetDialog(
                    dialog,
                    scope,
                    this,
                    LocalColorScheme.current
                )

                DialogShowType.FullScreen -> FullScreenDialog(dialog, scope, this)
                DialogShowType.Center -> ScreenCenterDialog(dialog, scope, this)
                DialogShowType.RightSheet -> RightSheetDialog(
                    dialog,
                    scope,
                    this,
                    LocalColorScheme.current
                )

                else -> {
                    // do nothing
                }
            }
        }
    }

    @Composable
    private fun RenderOverlayDialog(overlayDialog: IDialog?, scope: CoroutineScope) {
        when (overlayDialog?.showType) {
            DialogShowType.Snackbar -> SnackbarDialog(overlayDialog, scope, this)
            DialogShowType.Toast -> ToastDialog(overlayDialog, scope, this)
            else -> {
                // do nothing
            }
        }
    }
}

private fun DialogShowType.isOverlay(): Boolean =
    this == DialogShowType.Snackbar || this == DialogShowType.Toast
