package com.tencent.news.core.compose.scaffold

import com.tencent.kuikly.core.module.Module
import com.tencent.news.core.annotation.PlatformRawApi
import com.tencent.news.core.annotation.PlatformRawApiReason
import com.tencent.news.core.extension.toJson
import com.tencent.news.core.isHarmonyPlatform

/**
 * Author: joejhzhou
 * Date: 2025/3/13
 */
open class ComposeDialog : ComposePage() {

    override fun createExternalModules(): Map<String, Module>? {
        // 先继承父类 ComposePage 已注册的模块（含 BusinessModule），再追加 DialogModule。
        // 原实现用 DialogModule 作为 base map 再 putAll(original)，会让 original 覆盖同名键，
        // 虽然当前父类与 Dialog 无同名键冲突，但写法容易让 DialogModule 被后注册的父类模块覆盖，
        // 更重要的是语义不清晰。这里改为"父类为 base，子类追加"的标准合并顺序。
        val merged = super.createExternalModules()?.toMutableMap() ?: mutableMapOf()
        merged[NewsComposeModule.Dialog.moduleName] = DialogModule()
        return merged
    }

    protected open fun dialogName(): String = ""

    protected open fun buildCloseDialogParams(): Map<String, Any> {
        return mutableMapOf()
    }

    private fun buildBasicParams(animation: Boolean = false): MutableMap<String, Any> {
        return mutableMapOf<String, Any>(
            "animation" to animation,
            "moduleName" to NewsComposeModule.Dialog.moduleName
        ).apply {
            dialogName().takeIf { it.isNotEmpty() }?.let { put("dialogName", it) }
        }
    }

    open fun onCloseDialog() {
        onCloseDialog(true)
    }

    @OptIn(PlatformRawApi::class)
    @PlatformRawApiReason("鸿蒙不接受map，需要转成json string")
    open fun onCloseDialog(animation: Boolean = false) {
        val params = buildBasicParams(animation)
        params.putAll(buildCloseDialogParams())
        if (isHarmonyPlatform()) {
            getModule<DialogModule>(NewsComposeModule.Dialog.moduleName)?.onCloseDialog(params.toJson())
            return
        }
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