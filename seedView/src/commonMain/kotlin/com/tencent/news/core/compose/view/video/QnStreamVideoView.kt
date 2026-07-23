package com.tencent.news.core.compose.view.video

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.base.Attr
import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.kuikly.core.base.event.Event

@Composable
fun QnStreamVideo(
    modifier: Modifier = Modifier,
    onViewCreated: (Any) -> Unit
) {
    MakeKuiklyComposeNode(
        factory = {
            QnStreamVideoView()
        },
        modifier = modifier,
        viewInit = {},
        viewUpdate = {
            it.getViewAttr().apply {
                setProp("onViewCreated", onViewCreated)
            }
        }
    )
}

internal class QnStreamVideoView : DeclarativeBaseView<QnStreamVideoViewAttr, QnStreamVideoEvent>() {

    override fun createAttr(): QnStreamVideoViewAttr {
        return QnStreamVideoViewAttr()
    }

    override fun createEvent(): QnStreamVideoEvent {
        return QnStreamVideoEvent()
    }

    override fun viewName(): String {
        return "QnStreamVideoView"
    }
}

internal class QnStreamVideoViewAttr : Attr()

internal class QnStreamVideoEvent : Event()
