package com.tencent.news.core.compose.view.webview

import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * QnWebPageView 内部 DeclarativeBaseView 实现。
 *
 * 各端原生 View 通过 viewName() 返回的 "QnWebView" 进行匹配注册。
 */
internal class QnWebPageViewInternal : DeclarativeBaseView<QnWebPageViewAttr, QnWebPageViewEvent>() {

    override fun createAttr(): QnWebPageViewAttr {
        return QnWebPageViewAttr()
    }

    override fun createEvent(): QnWebPageViewEvent {
        return QnWebPageViewEvent()
    }

    override fun viewName(): String {
        return VIEW_NAME
    }

    /**
     * 加载指定 URL。
     */
    fun loadUrl(url: String) {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("loadUrl", url)
        }
    }

    /**
     * 使用 POST 方式加载指定 URL。
     */
    fun postUrl(url: String, postData: String) {
        val params = buildStringParamsJson(
            "url" to url,
            "postData" to postData,
        )
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("postUrl", params)
        }
    }

    /**
     * 加载 HTML 字符串。
     */
    fun loadHtml(html: String, baseUrl: String = "") {
        val params = buildStringParamsJson(
            "html" to html,
            "baseUrl" to baseUrl,
        )
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("loadHtml", params)
        }
    }

    /**
     * 执行 JavaScript 代码。
     */
    fun evaluateJavaScript(script: String, callback: ((String?) -> Unit)? = null) {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("evaluateJavaScript", script, callback?.let { cb ->
                { result: Any? ->
                    cb(parseStringCallbackResult(result))
                }
            })
        }
    }

    /**
     * 后退。
     */
    fun goBack() {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("goBack", null)
        }
    }

    /**
     * 前进。
     */
    fun goForward() {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("goForward", null)
        }
    }

    /**
     * 重新加载。
     */
    fun reload() {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("reload", null)
        }
    }

    /**
     * 查询是否可以后退。
     */
    fun canGoBack(callback: (Boolean) -> Unit) {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("canGoBack", null) { result: Any? ->
                callback(parseBooleanCallbackResult(result))
            }
        }
    }

    /**
     * 查询是否可以前进。
     */
    fun canGoForward(callback: (Boolean) -> Unit) {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("canGoForward", null) { result: Any? ->
                callback(parseBooleanCallbackResult(result))
            }
        }
    }

    /**
     * 通过 JSBridge 发送消息到 JS 端。
     */
    fun sendMessageToJS(method: String, params: String) {
        val script = KuiklyBridgeProtocol.buildEventScript(method, params)
        evaluateJavaScript(script)
    }

    /**
     * 解析原生 `callMethod` 返回的布尔结果。
     *
     * Kuikly 桥接层会把 native 方法回调值包装为 [JSONObject] 再分发到 common 层，因此
     * `canGoBack` / `canGoForward` 约定使用 `{ "value": true|false }` 承载结果；
     * 字符串分支仅用于兼容历史实现或本地 mock。
     */
    private fun parseBooleanCallbackResult(result: Any?): Boolean {
        return when (result) {
            is JSONObject -> result.optBoolean(CALLBACK_RESULT_KEY)
            is Boolean -> result
            is String -> result == CALLBACK_TRUE_VALUE
            else -> false
        }
    }

    /**
     * 解析原生 `evaluateJavaScript` 返回值。
     *
     * Harmony Kuikly 回调需使用 `{ "value": "..." }` 包装结果，避免裸字符串被桥接层当成
     * JSONObject 解析导致崩溃；字符串分支用于兼容 Android/iOS 或历史实现。
     */
    private fun parseStringCallbackResult(result: Any?): String? {
        return when (result) {
            is JSONObject -> result.optString(CALLBACK_RESULT_KEY)
            is String -> result
            else -> result?.toString()
        }
    }

    private fun buildStringParamsJson(vararg values: Pair<String, String>): String {
        return values.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"$key\":\"${WebViewJsScriptUtils.escapeJsString(value)}\""
        }
    }

    companion object {
        /** 组件对应的原生视图名称，各端注册时使用此名称。 */
        const val VIEW_NAME = "QnWebView"
        /** 原生方法回调中承载结果的字段名。 */
        private const val CALLBACK_RESULT_KEY = "value"
        /** 兼容历史字符串回调时使用的 true 字面量。 */
        private const val CALLBACK_TRUE_VALUE = "true"
    }
}
