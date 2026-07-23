package com.tencent.news.core.compose.view.webview

/**
 * QnWebPageView 命令式控制器。
 *
 * 适用场景：
 * 页面副作用需要触发 `evaluateJavaScript` 这类一次性命令时，通过 controller 直接调用，
 * 避免把命令建模为 Compose state 后再清空。
 */
class QnWebPageController internal constructor() {

    private var webPageView: QnWebPageViewInternal? = null
    private val pendingCommands = mutableListOf<(QnWebPageViewInternal) -> Unit>()

    /**
     * 让当前绑定的 WebView 执行 JavaScript。
     *
     * @param script 待执行脚本；空白脚本会被忽略。
     */
    fun evaluateJavaScript(script: String) {
        if (script.isBlank()) return
        performWebViewCommand { webView ->
            webView.evaluateJavaScript(script)
        }
    }

    /**
     * 让当前绑定的 WebView 执行 JavaScript，并把执行结果回调给调用方。
     */
    fun evaluateJavaScript(script: String, callback: (String?) -> Unit) {
        if (script.isBlank()) return
        performWebViewCommand { webView ->
            webView.evaluateJavaScript(script, callback)
        }
    }

    /**
     * 使用 POST 方式加载指定 URL。
     *
     * @param url 待加载 URL；空白 URL 会被忽略。
     * @param postData POST 请求体内容，按 UTF-8 编码传给各端原生 WebView。
     */
    fun postUrl(url: String, postData: String) {
        if (url.isBlank()) return
        performWebViewCommand { webView ->
            webView.postUrl(url, postData)
        }
    }

    /**
     * 让当前绑定的 WebView 执行网页内后退。
     *
     * 适用场景：
     * 页面收到 `ui.goBack` 后，先通过 [canGoBack] 判断网页内可后退，再调用该方法触发
     * 原生 WebView history 后退。若 WebView 尚未绑定，则延迟到绑定完成后执行。
     */
    fun goBack() {
        performWebViewCommand { webView ->
            webView.goBack()
        }
    }

    /**
     * 重新加载当前 WebView 页面。
     */
    fun reload() {
        performWebViewCommand { webView ->
            webView.reload()
        }
    }

    /**
     * 查询当前绑定的 WebView 是否存在可后退的网页 history。
     *
     * @param callback 查询结果回调；true 表示 WebView 可以网页内后退，false 表示需要由页面关闭兜底。
     */
    fun canGoBack(callback: (Boolean) -> Unit) {
        performWebViewCommand { webView ->
            webView.canGoBack(callback)
        }
    }

    /**
     * 绑定当前组合中的内部 WebView 实例。
     *
     * WebView 尚未绑定时收到的命令会在这里按顺序补执行，避免事件早于原生 View attach 时丢失。
     */
    internal fun bind(webPageView: QnWebPageViewInternal) {
        if (this.webPageView === webPageView) return
        this.webPageView = webPageView
        flushPendingCommands()
    }

    /**
     * 解绑即将离开组合的内部 WebView 实例。
     *
     * 只解绑当前持有的实例，避免旧实例 dispose 时误清掉新实例。
     */
    internal fun unbind(webPageView: QnWebPageViewInternal) {
        if (this.webPageView === webPageView) {
            this.webPageView = null
        }
    }

    /**
     * 执行需要依赖 WebView 绑定后的控制命令。
     *
     * controller 尚未绑定时先缓存命令，绑定完成后再按收到顺序补执行，避免一次性命令丢失或乱序。
     */
    private fun performWebViewCommand(command: (QnWebPageViewInternal) -> Unit) {
        val currentWebPageView = webPageView
        if (currentWebPageView == null) {
            pendingCommands.add(command)
        } else {
            command(currentWebPageView)
        }
    }

    /**
     * 补执行绑定前积压的 WebView 控制命令。
     *
     * 复制后再清空队列，避免执行命令时再次入队导致遍历状态不稳定。
     */
    private fun flushPendingCommands() {
        val currentWebPageView = webPageView ?: return
        if (pendingCommands.isEmpty()) return
        val commands = pendingCommands.toList()
        pendingCommands.clear()
        commands.forEach { command ->
            command(currentWebPageView)
        }
    }
}
