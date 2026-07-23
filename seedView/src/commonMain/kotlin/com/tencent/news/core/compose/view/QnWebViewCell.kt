package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.KuiklyDefaultMeasurePolicy
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.base.Attr
import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.safeToFloat

private const val HTML_URL = "html_url"
private const val STATE = "state" // 当前state，内部使用
private const val THEME = "applyTheme" // 切换主题 日夜间

@Composable
fun QnWebViewCell(
    modifier: Modifier = Modifier,
    state: WebState,
    url: String,
) {
    val isDark = isAppInDarkTheme()
    MakeKuiklyComposeNode<QnWebViewCell>(
        factory = {
            QnWebViewCell()
        },
        modifier = modifier,
        measurePolicy = KuiklyDefaultMeasurePolicy,
        viewInit = {},
        viewUpdate = {
            it.getViewAttr().run {
                with(HTML_URL, url)
                with(STATE, state)
                with(THEME, isDark)
            }
        }
    )
}

fun interface OnRatioListener {
    fun onRatioChanged(ratio: Float)
}

object QnWebCellAttr {
    const val SET_RATIO = "setRatioListener"

    /**
     * 只处理JSONObject和String类型，其他类型返回null
     */
    fun decode(value: Any?): String? {
        return when (value) {
            is JSONObject -> {
                val statusValue = value.optString("ratio")
                if (statusValue.isNotEmpty()) decode(statusValue) else null
            }

            is String -> value
            else -> null
        }
    }
}

class WebState {
    var ratio by mutableFloatStateOf(0f)
    val ratioListener: OnRatioListener = OnRatioListener {
        ratio = it
    }
}

private class WebViewCellViewAttr : Attr() {
    internal fun with(key: String, value: Any): WebViewCellViewAttr = this.apply {
        key with value
        if (value is WebState) {
            (view() as? QnWebViewCell)?.getViewEvent()?.let { event ->
                event.setRatioListener(value.ratioListener)
            }
        }
    }
}

private class WebViewCellEvent : Event() {
    internal fun setRatioListener(listener: OnRatioListener?) {
        if (listener != null) {
            register(QnWebCellAttr.SET_RATIO) {
                val ratioStr = QnWebCellAttr.decode(it) ?: return@register
                if (ratioStr.isNotNullOrEmpty()) {
                    listener.onRatioChanged(ratioStr.safeToFloat())
                }
            }
        } else {
            unRegister(QnWebCellAttr.SET_RATIO)
        }
    }
}

private class QnWebViewCell : DeclarativeBaseView<WebViewCellViewAttr, WebViewCellEvent>() {
    override fun createAttr(): WebViewCellViewAttr {
        return WebViewCellViewAttr()
    }

    override fun createEvent(): WebViewCellEvent {
        return WebViewCellEvent()
    }

    override fun viewName(): String {
        return "QnWebViewCell"
    }

}