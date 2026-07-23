package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.compose.adaptive.LocalAdaptiveDialogType
import com.tencent.news.core.compose.scaffold.theme.LocalColorScheme
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.platform.api.appPopBridge
import com.tencent.news.core.platform.api.appRouter
import com.tencent.news.core.pop.PopType
import com.tencent.news.core.router.contants.ComposeViewKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DialogController {

    private val dialogFlow = MutableStateFlow<IDialog?>(null)

    val isDialogShowing = dialogFlow.map { it != null }

    // 标记当前 controller 下新创建的 dialog 是否默认使用 Native 桥接弹窗
    // 使用时：dialog.apply { isNative = dialogController.isNative }
    var isNative = false
    
    /**
     * 显示弹窗
     * @param dialog 弹窗组件
     */
    suspend fun showDialog(dialog: IDialog) {
        dialogFlow.emit(dialog)
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
        content: @Composable () -> Unit
    ) {
        scope.launch {
            showDialog(object : IDialog() {
                override val showType: DialogShowType = showType
                override val content: @Composable (pageScope: CoroutineScope, controller: DialogController) -> Unit
                    get() = { _, _ -> content() }
            })
        }
    }

    /**
     * 关闭Dialog
     * @param dialog 弹窗组件，如果为空则关闭正在显示的弹窗
     */
    fun dismissDialog(dialog: IDialog?) {
        if (dialog == null || dialog == dialogFlow.value) {
            dialog?.onDismiss?.invoke()
            dialogFlow.value = null
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
        // 展示Dialog
        if (dialog != null && dialog?.isNative.isTrue()) {
            // 优先从 customPopType 获取 popType
            val popType: PopType = dialog?.customPopType ?: PopType.COMPOSE_BRIDGE_DIALOG
            // 桥接到native弹窗
            scope.launch {
                dialogFlow.value = null
                val args = NativeDialogHandlerController.put(dialog!!)
                appRouter().toComposeDialog(
                    LocalKmmContext,
                    popType,
                    ComposeViewKey.Dialog.NATIVE_BRIDGE,
                    args
                )
            }
        } else {
            CompositionLocalProvider(
                LocalAdaptiveDialogType provides dialog?.displayType
            ) {
                when (dialog?.showType) {
                    DialogShowType.BottomSheet -> BottomSheetDialog(
                        dialog,
                        scope,
                        this,
                        LocalColorScheme.current
                    )
                    DialogShowType.FullScreen -> FullScreenDialog(dialog, scope, this)
                    DialogShowType.Center -> ScreenCenterDialog(dialog, scope, this)
                    null -> {
                        // do nothing
                    }
                }
            }
        }

        // 分发dialog状态
        val isVisible = dialog != null
        glistener?.invoke(isVisible)
        // 处理compose弹窗和类MiniBar组件的冲突（弹窗时隐藏minibar,弹窗消失时显示minibar）
        appPopBridge()?.onPopVisibilityChanged(isVisible)

        // 关闭所有Dialog
        DisposableEffect(this) {
            onDispose { dismissDialog(null) }
        }
    }
}