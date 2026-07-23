package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.base.Attr
import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.kuikly.core.base.event.Event

private const val CONTENT_PROP = "content"
private const val FOREGROUND_COLOR_PROP = "foregroundColor"

private const val BACKGROUND_COLOR_PROP = "backgroundColor"

/**
 * @param foregroundColor 二维码前景色（如 "#000000"），null 时使用默认颜色
 */
@Composable
fun QrCode(
    content: String,
    modifier: Modifier = Modifier,
    foregroundColor: String? = null,
    backgroundColor: String? = null,
) {

    MakeKuiklyComposeNode<QrCodeView>(
        factory = {
            QrCodeView()
        },
        modifier = modifier,
        viewInit = { },
        viewUpdate = {
            it.getViewAttr().run {
                if (foregroundColor != null) {
                    with(FOREGROUND_COLOR_PROP, foregroundColor)
                }
                if (backgroundColor != null) {
                    with(BACKGROUND_COLOR_PROP, backgroundColor)
                }
                with(CONTENT_PROP, content)
            }
        },
    )
}

private class QrCodeView : DeclarativeBaseView<QrCodeViewAttr, QrCodeViewEvent>() {

    override fun createAttr(): QrCodeViewAttr {
        return QrCodeViewAttr()
    }

    override fun createEvent(): QrCodeViewEvent {
        return QrCodeViewEvent()
    }

    override fun viewName(): String {
        return "QnQrCodeView"
    }

    override fun isRenderView(): Boolean {
        return true
    }
}

private class QrCodeViewAttr : Attr() {
    internal fun with(key: String, value: Any): QrCodeViewAttr = this.apply {
        key with value
    }
}

private class QrCodeViewEvent() : Event()