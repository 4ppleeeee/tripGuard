package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.core.annotations.Page
import com.tencent.news.core.compose.scaffold.dialog.PopUpDialog
import com.tencent.news.core.compose.scaffold.registry.LocalDialogController
import com.tencent.news.core.router.contants.FrameworkViewKey

/**
 * Compose Dialog以native壳形式展示时，会跳转到这个页面显示真正的弹窗内容
 */
@Page(name = FrameworkViewKey.Dialog.NATIVE_BRIDGE)
internal class NativeDialogBridgePage : PopUpDialog() {

    // 缓存的dialogHandler，用于获取safeAreaBackgroundColor
    private var cachedDialogHandler: NativeDialogHandler? = null

    @Composable
    override fun getSafeAreaBackgroundColor(): Color? {
        // 尝试从页面参数获取dialogHandler
        val args = rememberedPageArgs<NativeDialogBridgePageArgs>()
        val handler = args?.dialogHandler ?: cachedDialogHandler ?: return null
        
        // 通过get方法获取dialog（不移除），并获取其safeAreaBackgroundColorProvider
        val dialog = NativeDialogHandlerController.get(handler)
        return dialog?.safeAreaBackgroundColorProvider?.invoke()
    }

    @Composable
    override fun DialogContent(modifier: Modifier, dismiss: () -> Unit) {
        val args = rememberedPageArgs<NativeDialogBridgePageArgs>() ?: return
        if (args.dialogHandler == 0) {
            dismiss()
            return
        }

        // 缓存dialogHandler
        cachedDialogHandler = args.dialogHandler

        val dialog = remember { NativeDialogHandlerController.remove(args.dialogHandler) }
        if (dialog == null) {
            dismiss()
            return
        }

        val controller = LocalDialogController.current

        LaunchedEffect(Unit) {
            dialog.isNative = false
            controller.showDialog(dialog)

            controller.isDialogShowing.collect {
                if (!it) {
                    dismiss()
                }
            }
        }
    }
}

