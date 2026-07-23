package com.tencent.news.core.compose.view.webview

import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/** QnWebPageView 事件，原生侧通过 KuiklyRenderCallback 触发。 */
class QnWebPageViewEvent : Event() {

    fun onPageStarted(handler: (url: String) -> Unit) {
        register(EVENT_PAGE_STARTED) {
            val params = it as? JSONObject ?: JSONObject()
            handler(params.optString("url", ""))
        }
    }

    fun onPageFinished(handler: (url: String) -> Unit) {
        register(EVENT_PAGE_FINISHED) {
            val params = it as? JSONObject ?: JSONObject()
            handler(params.optString("url", ""))
        }
    }

    fun onError(handler: (errorCode: Int, description: String) -> Unit) {
        register(EVENT_ERROR) {
            val params = it as? JSONObject ?: JSONObject()
            handler(
                params.optInt("errorCode", -1),
                params.optString("description", "Unknown error")
            )
        }
    }

    fun onReceiveTitle(handler: (title: String) -> Unit) {
        register(EVENT_RECEIVE_TITLE) {
            val params = it as? JSONObject ?: JSONObject()
            handler(params.optString("title", ""))
        }
    }

    fun onProgressChanged(handler: (progress: Int) -> Unit) {
        register(EVENT_PROGRESS_CHANGED) {
            val params = it as? JSONObject ?: JSONObject()
            handler(params.optInt("progress", 0))
        }
    }

    /** 原生拦截到 `jsbridge://` 时触发，参数为原始 URL（交给引擎处理）。 */
    fun onJsBridgeRequest(handler: (url: String) -> Unit) {
        register(EVENT_JS_BRIDGE_REQUEST) {
            val params = it as? JSONObject ?: JSONObject()
            handler(params.optString("url", ""))
        }
    }

    /** 原生拦截到业务 scheme(鸿蒙侧映射为 `qqnews://`)时触发，参数为原始 scheme URL。 */
    fun onSchemeRequest(handler: (url: String) -> Unit) {
        register(EVENT_SCHEME_REQUEST) {
            val params = it as? JSONObject ?: JSONObject()
            handler(params.optString("url", ""))
        }
    }

    /** JS 通过 JSBridge 通道发来的消息（JSON 字符串）。 */
    fun onMessage(handler: (message: String) -> Unit) {
        register(EVENT_MESSAGE) {
            val params = it as? JSONObject ?: JSONObject()
            handler(params.optString("message", ""))
        }
    }

    companion object {
        const val EVENT_PAGE_STARTED = "onPageStarted"
        const val EVENT_PAGE_FINISHED = "onPageFinished"
        const val EVENT_ERROR = "onError"
        const val EVENT_RECEIVE_TITLE = "onReceiveTitle"
        const val EVENT_PROGRESS_CHANGED = "onProgressChanged"
        const val EVENT_JS_BRIDGE_REQUEST = "onJsBridgeRequest"
        const val EVENT_SCHEME_REQUEST = "onSchemeRequest"
        const val EVENT_MESSAGE = "onMessage"
    }
}
