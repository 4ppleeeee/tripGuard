package com.tencent.news.core.compose.view.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode

/**
 * QnWebPageView 跨端全页 WebView 组件
 *
 * 使用 Kuikly 的 DeclarativeBaseView 模式，将各平台原生 WebView 暴露为统一的跨端组件。
 * 原生端需注册名为 "QnWebView" 的渲染视图实现。
 *
 * @param controller WebView 命令控制器，用于从 Compose 副作用中触发 `evaluateJavaScript` 等命令式能力。
 * @param safeAreaInsetBottom 页面侧读取到的底部安全区，Android 原生侧用于系统导航栏兜底适配。
 * @param teenResetAuthScene 是否为未成年人模式重置密码实名认证 WebView，原生侧仅在该场景放行可信认证域的媒体权限。
 * @param onJsBridgeRequest 原生 WebView 拦截到 `jsbridge://` URL 时触发，用于把旧 JSAPI 请求透传给业务 VM。
 * @param onSchemeRequest 原生 WebView 拦截到业务 scheme（如 `qqnews://`）时触发，用于把 scheme 透传给页面 VM。
 *
 * 使用示例：
 * ```kotlin
 * val controller = rememberQnWebPageController()
 *
 * QnWebPageView(
 *     modifier = Modifier.fillMaxSize(),
 *     controller = controller,
 *     src = "https://example.com",
 *     javaScriptEnabled = true,
 *     domStorageEnabled = true,
 *     onPageFinished = { url -> /* 页面加载完成 */ },
 *     onJsBridgeRequest = { url -> /* 收到 jsbridge:// JSAPI 请求 */ },
 *     onMessage = { message -> /* 收到 JS 消息 */ }
 * )
 * ```
 */
@Composable
fun QnWebPageView(
    modifier: Modifier = Modifier,
    controller: QnWebPageController,
    src: String? = null,
    htmlContent: String? = null,
    safeAreaInsetBottom: Float? = null,
    teenResetAuthScene: Boolean = false,
    javaScriptEnabled: Boolean = true,
    domStorageEnabled: Boolean = true,
    allowsInlineMediaPlayback: Boolean = true,
    userAgent: String? = null,
    bridgeScript: String? = null,
    onPageStarted: ((url: String) -> Unit)? = null,
    onPageFinished: ((url: String) -> Unit)? = null,
    onError: ((errorCode: Int, description: String) -> Unit)? = null,
    onReceiveTitle: ((title: String) -> Unit)? = null,
    onProgressChanged: ((progress: Int) -> Unit)? = null,
    onJsBridgeRequest: ((url: String) -> Unit)? = null,
    onSchemeRequest: ((url: String) -> Unit)? = null,
    onMessage: ((message: String) -> Unit)? = null,
) {
    val webPageViewInternal = remember { QnWebPageViewInternal() }

    DisposableEffect(controller, webPageViewInternal) {
        controller.bind(webPageViewInternal)
        onDispose {
            controller.unbind(webPageViewInternal)
        }
    }

    MakeKuiklyComposeNode(
        factory = {
            webPageViewInternal
        },
        modifier = modifier,
        viewInit = { },
        viewUpdate = {
            it.getViewAttr().run {
                teenResetAuthScene(teenResetAuthScene)
                userAgent?.let { ua -> userAgent(ua) }
                bridgeScript?.let { js -> bridgeScript(js) }
                src?.let { url -> src(url) }
                htmlContent?.let { html -> htmlContent(html) }
                safeAreaInsetBottom?.let { bottom -> safeAreaInsetBottom(bottom) }
                javaScriptEnabled(javaScriptEnabled)
                domStorageEnabled(domStorageEnabled)
                allowsInlineMediaPlayback(allowsInlineMediaPlayback)
            }
            it.getViewEvent().run {
                onPageStarted?.let { handler -> onPageStarted(handler) }
                onPageFinished?.let { handler -> onPageFinished(handler) }
                onError?.let { handler -> onError(handler) }
                onReceiveTitle?.let { handler -> onReceiveTitle(handler) }
                onProgressChanged?.let { handler -> onProgressChanged(handler) }
                onJsBridgeRequest?.let { handler -> onJsBridgeRequest(handler) }
                onSchemeRequest?.let { handler -> onSchemeRequest(handler) }
                onMessage?.let { handler -> onMessage(handler) }
            }
        },
    )
}

/**
 * 创建并记住一个 [QnWebPageController]。
 *
 * @return 当前组合生命周期内稳定的 WebView 命令控制器。
 */
@Composable
fun rememberQnWebPageController(): QnWebPageController {
    return remember { QnWebPageController() }
}
