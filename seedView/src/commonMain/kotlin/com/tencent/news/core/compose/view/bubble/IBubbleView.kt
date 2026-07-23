package com.tencent.news.core.compose.view.bubble

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope

/**
 * bubble 消失方式
 * Touch ----- touch的 up 事件时消失，兼容了[UnConsumedPointerInputModifier]
 * Click ----- 点击外层容器才消失
 */
enum class DismissMode {
    Touch,Click
}
/**
 * 气泡锚点
 * @param x 气泡左上角的x坐标
 * @param y 气泡左上角的y坐标
 */
data class BubbleAnchor(val x: Float, val y: Float)

/**
 * 气泡状态
 * @param anchor 气泡锚点
 * @param stayMillis 气泡停留时间，小于等于0则一直停留
 * @param disappearOnTouch 气泡是否在点击时消失
 */
data class BubbleViewState(
    val anchor: BubbleAnchor,
    val stayMillis: Long,
    val disappearOnTouch: Boolean,
    val dismissMode: DismissMode
)

/**
 * 气泡状态
 * @param anchor 气泡锚点
 * @param stayMillis 气泡停留时间，小于等于0则一直停留
 * @param disappearOnTouch 气泡是否在点击时消失
 */
fun mutableBubbleViewState(
    anchor: BubbleAnchor,
    stayMillis: Long = 0,
    disappearOnTouch: Boolean = true,
    dismissMode: DismissMode = DismissMode.Touch
): MutableState<BubbleViewState> {
    return mutableStateOf(BubbleViewState(anchor, stayMillis, disappearOnTouch, dismissMode))
}

typealias BubbleViewContent = @Composable (pageScope: CoroutineScope, controller: BubbleViewController) -> Unit

/**
 * 气泡组件
 */
interface IBubbleView {

    /**
     * 气泡状态
     */
    val state: MutableState<BubbleViewState> @Composable get

    /**
     * 气泡组件的UI，只描述自己，不用关心气泡是咋弹出来的
     */
    val content: BubbleViewContent
}