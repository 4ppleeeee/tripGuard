package com.tencent.news.core.compose.view.video

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.base.DeclarativeBaseView

import com.tencent.news.core.compose.view.video.invoker.IQnVideoNativeInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoControllerNativeInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoDataNativeInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoUiConfigNativeInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoVolumeNativeInvoker
import com.tencent.news.core.video.api.QnVideoLog

@Composable
fun QnVideo(
    modifier: Modifier = Modifier,
    state: QnVideoState
) {
    MakeKuiklyComposeNode<QnVideoView>(
        factory = {
            QnVideoView()
        },
        modifier = modifier,
        viewInit = {},
        viewUpdate = {
            it.getViewAttr().run {
                with(QnVideoAttr.SCENE, state.scene)
                with(QnVideoAttr.STATE, state)
            }
        }
    )
}


internal class QnVideoView : DeclarativeBaseView<QnVideoViewAttr, QnVideoEvent>() {

    private val controllerInvoker: IQnVideoNativeInvoker by lazy {
        QnVideoControllerNativeInvoker(
            this
        )
    }
    private val dataInvoker: IQnVideoNativeInvoker by lazy { QnVideoDataNativeInvoker(this) }
    private val volumeInvoker: IQnVideoNativeInvoker by lazy { QnVideoVolumeNativeInvoker(this) }
    private val uiConfigInvoker: IQnVideoNativeInvoker by lazy { QnVideoUiConfigNativeInvoker(this) }


    override fun createAttr(): QnVideoViewAttr {
        return QnVideoViewAttr()
    }

    override fun createEvent(): QnVideoEvent {
        return QnVideoEvent()
    }

    override fun viewName(): String {
        return "QnVideoView"
    }

    override fun didSetProp(propKey: String, propValue: Any) {
        super.didSetProp(propKey, propValue)
        QnVideoLog.log(
            "didSetProp propKey$propKey propValue$propValue"
        )
        if (propValue is QnVideoState) {
            // 初始化ui -> 绑定数据 -> 播放器控制 顺序不要随意改动
            uiConfigInvoker.bindAction(propValue)
            volumeInvoker.bindAction(propValue)
            dataInvoker.bindAction(propValue)
            controllerInvoker.bindAction(propValue)
        }
    }
}










