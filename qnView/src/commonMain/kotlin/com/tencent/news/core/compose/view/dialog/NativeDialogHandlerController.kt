package com.tencent.news.core.compose.view.dialog

import com.tencent.news.core.compose.platform.IComposePageArgs
import kotlinx.serialization.Serializable


internal typealias NativeDialogHandler = Int

@Serializable
internal class NativeDialogBridgePageArgs(val dialogHandler: NativeDialogHandler) : IComposePageArgs


internal object NativeDialogHandlerController {

    private val dialogs = mutableMapOf<NativeDialogHandler, IDialog>()

    private fun IDialog.getHandler() = this.hashCode()

    fun put(dialog: IDialog): NativeDialogBridgePageArgs {
        dialogs[dialog.getHandler()] = dialog
        return NativeDialogBridgePageArgs(dialog.getHandler())
    }

    fun get(handler: NativeDialogHandler): IDialog? = dialogs[handler]

    fun remove(handler: NativeDialogHandler): IDialog? = dialogs.remove(handler)
}