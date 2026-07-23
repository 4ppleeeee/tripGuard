package com.tencent.news.core.compose

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

@Deprecated("DON'T USE THIS")
typealias AndroidDialogModuleCallback = (Fragment, String, Any?) -> Any?

/**
 * Author: joejhzhou
 * Date: 2025/3/13
 */
class AndroidDialogModule @JvmOverloads constructor(
    private val fragment: Fragment,
    private val moduleCallback: AndroidDialogModuleCallback? = null
) : KuiklyRenderBaseModule() {
    companion object {
        const val ON_CLOSE_DIALOG = "onCloseDialog"
    }
    // 传输基本类型、数组、字符串
    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        when (method) {
            ON_CLOSE_DIALOG -> {
                (fragment as? DialogFragment)?.dismissAllowingStateLoss()
                moduleCallback?.invoke(fragment, method, params)
                return null
            }
            // 不要再新增方法，Module中的方法调用要收敛到其内部，不能分发出去
            else -> return moduleCallback?.invoke(fragment, method, params) ?: super.call(
                method,
                params,
                callback
            )
        }
    }

    // 传输Json（会被序列化为Json字符串）
    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return super.call(method, params, callback)
    }
}