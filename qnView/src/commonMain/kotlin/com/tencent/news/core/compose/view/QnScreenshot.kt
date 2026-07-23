package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

const val SCREENSHOT_PATH = "path"

@Composable
fun QnScreenshot(
    modifier: Modifier = Modifier,
    state: ScreenshotState,
    content: @Composable () -> Unit,
) {

    DisposableEffect(state) {
        onDispose {
            state.path = null
        }
    }

    MakeKuiklyComposeNode<QnScreenshotView>(
        factory = {
            QnScreenshotView()
        },
        modifier = modifier,
        viewInit = { },
        viewUpdate = {
            it.getViewAttr().run {
                with("state", state)
            }
        },
        content = content
    )
}

private class QnScreenshotView() :
    ViewContainer<ScreenshotViewAttr, ScreenshotViewEvent>() {

    override fun createAttr(): ScreenshotViewAttr {
        return ScreenshotViewAttr()
    }

    override fun createEvent(): ScreenshotViewEvent {
        return ScreenshotViewEvent()
    }

    override fun viewName(): String {
        return "QnScreenshotView"
    }

    override fun didSetProp(propKey: String, propValue: Any) {
        super.didSetProp(propKey, propValue)
        if (propValue is ScreenshotState) {
            propValue.callback = {
                take(it, propValue)
            }
        }
    }

    private fun take(scope: CoroutineScope, state: ScreenshotState) {
        callRenderViewMethod(methodName = "take") {
            val path = it?.optString(SCREENSHOT_PATH)
            state.path = path
            scope.launch {
                state.flow.emit(path)
            }
        }
    }
}


private class ScreenshotViewAttr : ContainerAttr() {
    fun with(key: String, value: Any): ScreenshotViewAttr = this.apply {
        key with value
    }
}

private class ScreenshotViewEvent() : Event()

@Composable
fun rememberScreenshotState() = remember {
    mutableStateOf(ScreenshotState())
}

@Stable
class ScreenshotState {

    var path: String? = null

    var flow: MutableSharedFlow<String?> = MutableSharedFlow()

    var callback: ((scope: CoroutineScope) -> Unit)? = null

    fun take(scope: CoroutineScope): Flow<String?> {
        callback?.invoke(scope)
        return flow
    }
}
