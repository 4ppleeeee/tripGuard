package com.tencent.news.core.compose

import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

typealias AndroidViewModuleCallback = (String, Any?) -> Any?

class AndroidViewModule @JvmOverloads constructor(
    private val moduleCallback: AndroidViewModuleCallback? = null
) : KuiklyRenderBaseModule() {
    companion object {
        const val ON_CLOSE_DIALOG = "onCloseDialog"
    }

    // 传输基本类型、数组、字符串
    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?) =
        moduleCallback?.invoke(method, params) ?: super.call(method, params, callback)
}