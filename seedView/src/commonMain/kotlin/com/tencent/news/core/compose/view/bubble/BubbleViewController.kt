package com.tencent.news.core.compose.view.bubble

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class BubbleViewController {

    private val bubbles = MutableStateFlow<IBubbleView?>(null)


    /**
     * 设置气泡消失回调
     * @param callback 回调函数，参数为消失的气泡对象
     */
    var onBubbleDisappear: ((bubbleView: IBubbleView?) -> Unit)? = null

    /**
     * 显示气泡组件
     * @param bubbleView 气泡组件
     */
    suspend fun showBubbleView(bubbleView: IBubbleView) {
        bubbles.emit(bubbleView)
    }

    /**
     * 主动隐藏气泡
     */
    fun dismissBubbleView(bubbleView: IBubbleView?) {
        if (bubbleView == null || bubbleView == bubbles.value) {
            bubbles.value = null
        }
    }

    /**
     * 监听气泡组件状态并展示
     */
    @Composable
    fun CollectBubbleState() {
        val bubble by bubbles.collectAsState()
        bubble?.let {
            val config by it.state
            val scope = rememberCoroutineScope()
            BubbleView(
                start = config.anchor.x,
                top = config.anchor.y,
                stayMillis = config.stayMillis,
                disappearOnTouch = config.disappearOnTouch,
                dismissMode = config.dismissMode,
                onDisappear = {
                    this.dismissBubbleView(it)
                    // 触发消失回调
                    onBubbleDisappear?.invoke(it)
                },
                content = { it.content.invoke(scope, this) }
            )
        }
        DisposableEffect(bubbles) {
            onDispose {
                bubbles.value = null
            }
        }
    }
}

