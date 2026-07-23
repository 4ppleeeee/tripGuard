package com.tencent.news.core.compose.view.bubble

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.input.pointer.PointerEventType
import com.tencent.news.core.compose.scaffold.modifiers.offset
import com.tencent.news.core.compose.view.pointerInputFilter
import kotlinx.coroutines.delay

/**
 *
 * 气泡组件
 * @param start 气泡组件在屏幕中的起始位置x坐标
 * @param top 气泡组件在屏幕中的起始位置y坐标
 * @param disappearOnTouch 气泡组件是否在点击时消失
 * @param stayMillis 气泡组件停留时间，小于等于0则一直停留
 * @param content 气泡组件内容
 */
@Composable
internal fun BubbleView(
    start: Float,
    top: Float,
    disappearOnTouch: Boolean = true,
    dismissMode: DismissMode,
    stayMillis: Long = 0L,
    onDisappear: () -> Unit,
    content: @Composable () -> Unit,
) {

    val latestOnDisappear by rememberUpdatedState(onDisappear)

    Box(
        modifier = Modifier.fillMaxSize()
            .let {
                when (dismissMode) {
                    DismissMode.Click -> {
                        it.clickable {
                            latestOnDisappear()
                        }
                    }

                    DismissMode.Touch -> {
                        it.pointerInputFilter(
                            onTouchEvent = { pointerEvent ->
                                if (disappearOnTouch && pointerEvent.type == PointerEventType.Release) {
                                    latestOnDisappear()
                                }
                            }
                        )
                    }
                }
            }

    ) {
        Box(modifier = Modifier.fillMaxSize().offset(x = start, y = top)) {
            content()
        }
    }

    LaunchedEffect(Unit) {
        if (stayMillis > 0) {
            delay(stayMillis)
            onDisappear()
        }
    }
}