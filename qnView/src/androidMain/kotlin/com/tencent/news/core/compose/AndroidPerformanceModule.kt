package com.tencent.news.core.compose

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.render.android.const.KRCssConst
import com.tencent.kuikly.core.render.android.css.ktx.getViewData
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

internal class AndroidPerformanceModule(private val onFirstFrame: (() -> Unit)? = null) : KuiklyRenderBaseModule() {
    // 传输基本类型、数组、字符串
    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        when (method) {
            "onPageFirstFrameRendered" -> {
                onPageFirstFrameRendered()
                return null
            }
            "firePreClick" -> {
                firePreClick(params as String)
                return null
            }

            else -> {
                return super.call(method, params, callback)
            }
        }
    }

    // 传输Json（会被序列化为Json字符串）
    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return super.call(method, params, callback)
    }

    private fun onPageFirstFrameRendered() {
        onFirstFrame?.invoke()
    }

    private fun firePreClick(params: String) {
        val nativeRef = JSONObject(params).optInt("nativeRef", -1)
        if (nativeRef >= 0) {
            kuiklyRenderContext?.getView(nativeRef)?.apply {
                this.getViewData<KuiklyRenderCallback>(KRCssConst.PRE_CLICK)?.invoke(mapOf<String, String>())
            }
        }
    }
}