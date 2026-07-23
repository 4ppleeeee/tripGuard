package com.tencent.kmm.demo.view.webview

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.tencent.kmm.demo.library.log.WsLogger
import org.json.JSONArray
import org.json.JSONObject

/**
 * Android 腾讯新闻 JSAPI 兼容桥。
 *
 * `qqnews-jsapi` 在 Android WebView 中会先探测 `window.TencentNewsJsBridge`，
 * 再通过 `bridgeCall(jsbridge://get_with_json_data?json=...)` 进入客户端。
 */
class AndroidTencentNewsJsBridge(
    private val onJsBridgeRequest: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun bridgeCall(url: String?): String {
        val rawUrl = url.orEmpty()
        WsLogger.i(TAG, "TencentNewsJsBridge.bridgeCall rawUrl=$rawUrl")
        val targetUrl = rawUrl.toTencentNewsJsBridgeUrl()
        if (targetUrl.isNullOrBlank()) {
            WsLogger.w(TAG, "TencentNewsJsBridge.bridgeCall ignored rawUrl=$rawUrl")
            return RESPONSE_OK
        }
        WsLogger.i(TAG, "TencentNewsJsBridge.bridgeCall dispatch targetUrl=$targetUrl")
        mainHandler.post {
            onJsBridgeRequest(targetUrl)
        }
        return RESPONSE_OK
    }

    private fun String.toTencentNewsJsBridgeUrl(): String? {
        if (!startsWith(SCHEME_JS_BRIDGE, ignoreCase = true)) {
            return null
        }
        if (!startsWith(LEGACY_BRIDGE_PREFIX, ignoreCase = true)) {
            return this
        }
        return runCatching {
            val json = Uri.parse(this).getQueryParameter(QUERY_JSON).orEmpty()
            val request = JSONObject(json)
            val method = request.optString(KEY_METHOD)
            if (method.isBlank()) {
                return@runCatching null
            }

            val payload = request.optJSONArray(KEY_ARGS).toPayload(method)
            val callbackId = request.optString(KEY_CALLBACK_ID)
            if (callbackId.isNotBlank()) {
                payload.put(KEY_CALLBACK, callbackId)
                payload.put(KEY_ON_CALLBACK, callbackId)
            }
            "$TENCENT_NEWS_BRIDGE_PREFIX${Uri.encode(method)}?$QUERY_PARAMS=${Uri.encode(payload.toString())}"
        }.getOrElse { error ->
            WsLogger.e(TAG, "parse TencentNews legacy bridge failed, rawUrl=$this", error)
            null
        }
    }

    private fun JSONArray?.toPayload(method: String): JSONObject {
        val firstArg = this?.optString(ARG_INDEX_FIRST).orEmpty()
        val objectPayload = firstArg.takeIf { it.isNotBlank() }
            ?.let { runCatching { JSONObject(it) }.getOrNull() }
        if (objectPayload != null) {
            return objectPayload
        }
        if (!method.equals(METHOD_SHARE, ignoreCase = true)) {
            return JSONObject()
        }
        return JSONObject().apply {
            firstArg.takeIf { it.isNotBlank() }?.let { put(KEY_DESTINATION, it) }
            this@toPayload?.optString(ARG_INDEX_SECOND)
                ?.takeIf { it.isNotBlank() }
                ?.let { item ->
                    put(KEY_ITEM, runCatching { JSONObject(item) }.getOrNull() ?: item)
                }
        }
    }

    companion object {
        private const val TAG = "AndroidTencentNewsJsBridge"
        private const val SCHEME_JS_BRIDGE = "jsbridge://"
        private const val LEGACY_BRIDGE_PREFIX = "jsbridge://get_with_json_data"
        private const val TENCENT_NEWS_BRIDGE_PREFIX = "jsbridge://TencentNews/"
        private const val QUERY_JSON = "json"
        private const val QUERY_PARAMS = "p"
        private const val METHOD_SHARE = "share"
        private const val KEY_METHOD = "method"
        private const val KEY_ARGS = "args"
        private const val KEY_CALLBACK_ID = "callbackId"
        private const val KEY_CALLBACK = "callback"
        private const val KEY_ON_CALLBACK = "onCallback"
        private const val KEY_DESTINATION = "destination"
        private const val KEY_ITEM = "item"
        private const val ARG_INDEX_FIRST = 0
        private const val ARG_INDEX_SECOND = 1
        private const val RESPONSE_OK = """{"code":200,"result":""}"""
    }
}
