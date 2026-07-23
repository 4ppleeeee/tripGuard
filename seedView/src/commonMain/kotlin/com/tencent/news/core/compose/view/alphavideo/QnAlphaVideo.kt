package com.tencent.news.core.compose.view.alphavideo

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.base.DeclarativeBaseView
import com.tencent.news.core.compose.view.alphavideo.invoker.AlphaVideoControllerNativeInvoker
import com.tencent.news.core.compose.view.alphavideo.invoker.IAlphaVideoNativeInvoker

/**
 * 透明视频播放器 Composable 组件
 *
 * 基于 Kuikly 框架的声明式透明视频播放器，支持三端通用。
 *
 * @param modifier Compose 修饰符
 * @param state 播放器状态，控制播放生命周期和数据绑定
 *
 * 用法：
 * ```kotlin
 * val state = remember {
 *     QnAlphaVideoState(
 *         scope = rememberCoroutineScope(),
 *         playInfo = QnAlphaVideoPlayInfo(
 *             url = "https://...",
 *             formatType = AlphaVideoFormatType.RGB_ALPHA_2_1,
 *             scaleType = AlphaVideoScaleType.CENTER_CROP
 *         )
 *     )
 * }
 *
 * QnAlphaVideo(
 *     modifier = Modifier.size(300.dp),
 *     state = state
 * )
 * ```
 */
@Composable
fun QnAlphaVideo(
    modifier: Modifier = Modifier,
    state: QnAlphaVideoState,
) {
    MakeKuiklyComposeNode<QnAlphaVideoView>(
        factory = {
            QnAlphaVideoView()
        },
        modifier = modifier,
        viewInit = {},
        viewUpdate = {
            it.getViewAttr().run {
                with(QnAlphaVideoAttr.DATA, state.playInfo)
                with(QnAlphaVideoAttr.STATE, state)
            }
        }
    )
}

/**
 * 透明视频播放器 DeclarativeBaseView
 *
 * 内部持有 NativeInvoker 实例，在 didSetProp 中通过 bindAction
 * 将 Compose Invoker 的 action 闭包与 NativeInvoker 的方法绑定，
 * 建立 Compose → Native 的双向通信链路。
 */
internal class QnAlphaVideoView : DeclarativeBaseView<QnAlphaVideoViewAttr, QnAlphaVideoEvent>() {

    private val controllerInvoker: IAlphaVideoNativeInvoker by lazy {
        AlphaVideoControllerNativeInvoker(this)
    }

    override fun createAttr(): QnAlphaVideoViewAttr {
        return QnAlphaVideoViewAttr()
    }

    override fun createEvent(): QnAlphaVideoEvent {
        return QnAlphaVideoEvent()
    }

    override fun viewName(): String {
        return "QnAlphaVideoView"
    }

    override fun didSetProp(propKey: String, propValue: Any) {
        super.didSetProp(propKey, propValue)
        if (propValue is QnAlphaVideoState) {
            controllerInvoker.bindAction(propValue)
        }
    }
}
