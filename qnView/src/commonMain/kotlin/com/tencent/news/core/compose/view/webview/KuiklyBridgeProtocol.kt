package com.tencent.news.core.compose.view.webview

/**
 * Kuikly WebView 通用 JSBridge 协议定义。
 *
 * 这层只负责 window.KuiklyBridge 的 callNative / response / event 通道，
 * 不包含腾讯新闻旧 JSAPI 兼容逻辑。
 */
object KuiklyBridgeProtocol {

    /** Bridge 对象名，JS 端通过 window.KuiklyBridge 访问。 */
    const val BRIDGE_NAME = "KuiklyBridge"

    /** Native 端消息处理器名称。 */
    const val NATIVE_HANDLER_NAME = "KuiklyNativeHandler"

    // ---- 消息 JSON Key ----
    const val KEY_CALL_ID = "callId"
    const val KEY_METHOD = "method"
    const val KEY_PARAMS = "params"
    const val KEY_RESULT = "result"
    const val KEY_ERROR = "error"
    const val KEY_TYPE = "type"

    // ---- 消息类型 ----
    /** JS 调用 Native 方法。 */
    const val TYPE_CALL = "call"
    /** Native 返回结果给 JS。 */
    const val TYPE_RESPONSE = "response"
    /** Native 主动推送消息给 JS。 */
    const val TYPE_EVENT = "event"

    /**
     * Kuikly WebView 消息桥脚本。
     *
     * 定义 window.KuiklyBridge 对象，提供：
     * - callNative(method, params): Promise，JS 调用 Native 方法。
     * - _onNativeResponse(callId, result, error)，Native 回调 Promise。
     * - _onNativeEvent(data)，Native 主动推送事件。
     * - registerHandler(name, handler)，JS 端注册消息处理器。
     * - _callbacks 超时清理机制（30 秒）。
     */
    const val KUIKLY_BRIDGE_JS_CODE = """
(function() {
    if (window.KuiklyBridge) return;

    var _callId = 0;
    var _callbacks = {};
    var _handlers = {};
    var CALLBACK_TIMEOUT = 30000;

    function cleanupCallback(id) {
        if (_callbacks[id]) {
            _callbacks[id].reject(new Error('Bridge call timeout'));
            delete _callbacks[id];
        }
    }

    window.KuiklyBridge = {
        callNative: function(method, params) {
            return new Promise(function(resolve, reject) {
                var id = 'cb_' + (_callId++);
                _callbacks[id] = { resolve: resolve, reject: reject };
                var timer = setTimeout(function() { cleanupCallback(id); }, CALLBACK_TIMEOUT);
                _callbacks[id]._timer = timer;
                var message = JSON.stringify({
                    type: 'call',
                    callId: id,
                    method: method,
                    params: params || {}
                });
                window.KuiklyBridge._postMessage(message);
            });
        },

        _onNativeResponse: function(callId, result, error) {
            var cb = _callbacks[callId];
            if (cb) {
                if (cb._timer) clearTimeout(cb._timer);
                if (error) {
                    cb.reject(new Error(error));
                } else {
                    cb.resolve(result);
                }
                delete _callbacks[callId];
            }
        },

        _onNativeEvent: function(data) {
            var eventName = data.method || 'message';
            var handler = _handlers[eventName];
            if (handler) {
                handler(data.params);
            }
        },

        registerHandler: function(name, handler) {
            _handlers[name] = handler;
        },

        _postMessage: function(message) {
            // 各平台实现不同，由原生端覆盖此方法
            // Android: KuiklyNativeHandler.postMessage(message)
            // iOS: webkit.messageHandlers.KuiklyNativeHandler.postMessage(message)
            // OHOS: KuiklyNativeHandler.postMessage(message)
        }
    };
})();
"""

    /** 旧默认注入脚本别名；业务 JSAPI 兼容脚本由页面扩展按需提供。 */
    const val BRIDGE_JS_CODE = KUIKLY_BRIDGE_JS_CODE

    /**
     * Android 平台的 postMessage 桥接脚本。
     * 通过 JavascriptInterface 传递消息。
     */
    const val ANDROID_POST_MESSAGE_JS = """
(function() {
    window.KuiklyBridge._postMessage = function(message) {
        window.KuiklyNativeHandler.postMessage(message);
    };
})();
"""

    /**
     * iOS 平台的 postMessage 桥接脚本。
     * 通过 WKScriptMessageHandler 传递消息。
     */
    const val IOS_POST_MESSAGE_JS = """
(function() {
    window.KuiklyBridge._postMessage = function(message) {
        window.webkit.messageHandlers.KuiklyNativeHandler.postMessage(message);
    };
})();
"""

    /**
     * OHOS 平台的 postMessage 桥接脚本。
     * 通过 javaScriptProxy 传递消息。
     */
    const val OHOS_POST_MESSAGE_JS = """
(function() {
    window.KuiklyBridge._postMessage = function(message) {
        window.KuiklyNativeHandler.postMessage(message);
    };
})();
"""

    /**
     * 构建 Native 回调 JS 的调用脚本。
     *
     * @param callId 调用 ID。
     * @param result 结果 JSON 字符串（可为 null）。
     * @param error 错误信息（可为 null）。
     */
    fun buildResponseScript(callId: String, result: String?, error: String?): String {
        val resultStr = result ?: "null"
        val errorStr = if (error != null) "'${WebViewJsScriptUtils.escapeJsString(error)}'" else "null"
        val escapedCallId = WebViewJsScriptUtils.escapeJsString(callId)
        return "window.KuiklyBridge._onNativeResponse('$escapedCallId', $resultStr, $errorStr);"
    }

    /**
     * 构建 Native 主动推送事件的 JS 脚本。
     *
     * @param method 事件方法名。
     * @param params 参数 JSON 字符串。
     */
    fun buildEventScript(method: String, params: String): String {
        val escapedMethod = WebViewJsScriptUtils.escapeJsString(method)
        return "window.KuiklyBridge._onNativeEvent({method:'$escapedMethod',params:$params});"
    }
}
