package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController

// 监听一个State，注册dialog弹窗的快捷工具：
// （dialog 目前的口径是：每次show的时候都重建一次）
@Composable
fun RegisterShowDialog(showState: State<Boolean>?, dialog: () -> IDialog) {
    showState ?: return

    val controller = LocalDialogController.current
    val scope = rememberCoroutineScope()

    // 用这个state记录正在展示的dialog，dismiss时使用
    // 注意不要直接remember记录dialog，会过早创建，且冗余的派发一次onDismiss
    val showingDialog = remember { mutableStateOf<IDialog?>(null) }

    val showDialog by showState
    LaunchedEffect(showDialog) {
        if (showDialog) {
            val dialog = dialog()
            showingDialog.value = dialog
            controller.showDialog(scope, dialog)
        } else {
            val dialog = showingDialog.value
            if (dialog != null) {
                controller.dismissDialog(dialog)
                showingDialog.value = null
            }
        }
    }
}