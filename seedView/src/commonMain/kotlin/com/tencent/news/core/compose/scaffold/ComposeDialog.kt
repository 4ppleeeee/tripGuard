package com.tencent.news.core.compose.scaffold

import com.tencent.kuikly.core.module.Module

/**
 * Author: joejhzhou
 * Date: 2025/3/13
 */
abstract class ComposeDialog : ComposePage() {

    override fun createExternalModules(): Map<String, Module>? {
        val original = super.createExternalModules()
        return hashMapOf<String, Module>(NewsComposeModule.Dialog.moduleName to DialogModule()).apply {
            if (original != null) {
                putAll(original)
            }
        }
    }

    open fun onCloseDialog() {
        onCloseDialog(true)
    }

    open fun onCloseDialog(animation: Boolean = false) {
        val params = mapOf<String, Any>(
            "animation" to animation
        )
        getModule<DialogModule>(NewsComposeModule.Dialog.moduleName)?.onCloseDialog(params)
    }
}

class DialogModule : Module() {

    fun onCloseDialog(param: Any?) {
        toNative(
            keepCallbackAlive = false,
            methodName = "onCloseDialog",
            param = param,
        )
    }

    override fun moduleName(): String = NewsComposeModule.Dialog.moduleName
}