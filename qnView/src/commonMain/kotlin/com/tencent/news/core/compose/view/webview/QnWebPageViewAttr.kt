package com.tencent.news.core.compose.view.webview

import com.tencent.kuikly.core.base.Attr

/** QnWebPageView 属性，通过 `key with value` 透传到各平台原生 WebView。 */
class QnWebPageViewAttr : Attr() {

    fun src(url: String): QnWebPageViewAttr {
        PROP_SRC with url
        return this
    }

    fun htmlContent(html: String): QnWebPageViewAttr {
        PROP_HTML_CONTENT with html
        return this
    }

    fun javaScriptEnabled(enabled: Boolean): QnWebPageViewAttr {
        PROP_JAVASCRIPT_ENABLED with enabled.toAttrBoolean()
        return this
    }

    fun userAgent(ua: String): QnWebPageViewAttr {
        PROP_USER_AGENT with ua
        return this
    }

    fun bridgeScript(script: String): QnWebPageViewAttr {
        PROP_BRIDGE_SCRIPT with script
        return this
    }

    fun domStorageEnabled(enabled: Boolean): QnWebPageViewAttr {
        PROP_DOM_STORAGE_ENABLED with enabled.toAttrBoolean()
        return this
    }

    fun allowsInlineMediaPlayback(allowed: Boolean): QnWebPageViewAttr {
        PROP_ALLOWS_INLINE_MEDIA with allowed.toAttrBoolean()
        return this
    }

    /** 底部安全区(dp)。原生侧在该值为 0 时用系统 WindowInsets 兜底，避免 H5 底部按钮被导航栏遮挡。 */
    fun safeAreaInsetBottom(bottomDp: Float): QnWebPageViewAttr {
        PROP_SAFE_AREA_INSET_BOTTOM with bottomDp.toString()
        return this
    }

    /** 未成年实名认证场景(原生侧据此放行可信认证域媒体权限)；BonBon 不用，保留为透传。 */
    fun teenResetAuthScene(enabled: Boolean): QnWebPageViewAttr {
        PROP_TEEN_RESET_AUTH_SCENE with enabled.toAttrBoolean()
        return this
    }

    internal fun with(key: String, value: Any): QnWebPageViewAttr = this.apply {
        key with value
    }

    private fun Boolean.toAttrBoolean(): String {
        return if (this) "true" else "false"
    }

    companion object {
        /** WebView URL 属性名。 */
        internal const val PROP_SRC = "src"
        /** HTML 字符串内容属性名。 */
        internal const val PROP_HTML_CONTENT = "htmlContent"
        /** 是否启用 JavaScript 的属性名。 */
        internal const val PROP_JAVASCRIPT_ENABLED = "javaScriptEnabled"
        /** 是否启用 DOM Storage 的属性名。 */
        internal const val PROP_DOM_STORAGE_ENABLED = "domStorageEnabled"
        /** 是否允许内联媒体播放的属性名。 */
        internal const val PROP_ALLOWS_INLINE_MEDIA = "allowsInlineMediaPlayback"
        /** 自定义 User-Agent 的属性名。 */
        internal const val PROP_USER_AGENT = "userAgent"
        /** 桥接注入脚本属性名（common 下发业务选择的 JSBridge 脚本，原生页面加载后注入）。 */
        internal const val PROP_BRIDGE_SCRIPT = "bridgeScript"
        /** 页面侧读取到的底部安全区，单位 dp。Android 原生侧会用它判断是否需要系统 inset 兜底。 */
        internal const val PROP_SAFE_AREA_INSET_BOTTOM = "safeAreaInsetBottom"
        /** 是否为未成年人模式重置密码实名认证 WebView。 */
        internal const val PROP_TEEN_RESET_AUTH_SCENE = "teenResetAuthScene"
    }
}
