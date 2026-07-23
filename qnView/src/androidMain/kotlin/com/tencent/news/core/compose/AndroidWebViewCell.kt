package com.tencent.news.core.compose

import android.content.Context
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport

/**
 * WebViewCell 抽象基类
 * 用于 Feeds 流中的 H5 WebCell 卡片
 *
 * 参考 [AndroidQrCodeView] 设计模式：
 * - SDK 层定义抽象基类和 prop 分发
 * - 宿主层实现具体的 WebView 创建/加载/复用逻辑
 */
abstract class AndroidWebViewCell(context: Context) : FrameLayout(context), IKuiklyRenderViewExport {
    override fun setProp(propKey: String, propValue: Any): Boolean {
        when (propKey) {
            PROP_HTML_URL -> {
                val url = propValue as? String ?: return false
                loadUrl(url)
            }
            PROP_USER_INTERACTION_ENABLED -> {
                val enabled = propValue as? Boolean ?: true
                setUserInteractionEnabled(enabled)
            }
            else -> return super.setProp(propKey, propValue)
        }
        return true
    }

    /**
     * 加载指定 URL
     * 宿主实现需要处理：URL 安全校验、isnm 参数追加、复用池管理等
     */
    abstract fun loadUrl(url: String)

    /**
     * 设置用户交互是否启用
     */
    abstract fun setUserInteractionEnabled(enabled: Boolean)

    companion object {
        const val PROP_HTML_URL = "html_url"
        const val PROP_USER_INTERACTION_ENABLED = "user_interaction_enabled"
    }
}
