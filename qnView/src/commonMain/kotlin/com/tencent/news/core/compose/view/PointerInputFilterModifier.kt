package com.tencent.news.core.compose.view

import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Size
import com.tencent.kuikly.compose.ui.input.pointer.PointerEvent
import com.tencent.kuikly.compose.ui.input.pointer.PointerEventPass
import com.tencent.kuikly.compose.ui.input.pointer.PointerInputFilter
import com.tencent.kuikly.compose.ui.input.pointer.PointerInputModifier
import com.tencent.kuikly.compose.ui.input.pointer.isOutOfBounds
import com.tencent.kuikly.compose.ui.unit.IntSize
import com.tencent.kuikly.compose.ui.util.fastAll

/**
 * 事件分发回调，如果要消费事件，则直接调用[PointerEvent.changes.fastForEach { it.consume() }]
 */
typealias OnTouchEvent = (PointerEvent) -> Unit
typealias OnTouchCancel = () -> Unit

/**
 * 元素接收未消费事件，且允许将未消费事件分发给兄弟节点，而不是直接分发给父节点。
 * @param pass 事件分发类型，默认为[PointerEventPass.Final]
 * @param onTouchEvent 事件处理回调
 * @param onTouchCancel 事件取消回调，参考[PointerInputFilter.onCancel]
 */
internal fun Modifier.pointerInputFilter(
    pass: PointerEventPass = PointerEventPass.Final,
    onTouchEvent: OnTouchEvent,
    onTouchCancel: OnTouchCancel = {}
): Modifier = this.then(UnConsumedPointerInputModifier(pass, onTouchEvent, onTouchCancel))

/**
 * 元素接收未消费事件但不消费事件，且未消费事件分发给兄弟节点。
 * @param pass 事件分发类型，默认为[PointerEventPass.Final]
 * @param onTouchEvent 事件处理回调
 */
private class UnConsumedPointerInputModifier(
    private val pass: PointerEventPass = PointerEventPass.Final,
    private val onTouchEvent: OnTouchEvent,
    private val onTouchCancel: OnTouchCancel
) : PointerInputModifier {

    override val pointerInputFilter: PointerInputFilter =
        UnConsumedPointerInputFilter(pass, onTouchEvent, onTouchCancel)
}

private class UnConsumedPointerInputFilter(
    private val pass: PointerEventPass = PointerEventPass.Final,
    private val onTouchEvent: OnTouchEvent,
    private val onTouchCancel: OnTouchCancel
) : PointerInputFilter() {

    override fun onCancel() {
        onTouchCancel()
    }

    // 允许将未消费事件分发给兄弟节点，而不是直接给父节点
    override val shareWithSiblings: Boolean = true

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        // 不在当前元素内
        if (pointerEvent.changes.fastAll { it.isOutOfBounds(bounds, Size.Zero) }) {
            return
        }

        // 满足Pass
        if (pass == this@UnConsumedPointerInputFilter.pass) {
            onTouchEvent(pointerEvent)
        }
    }
}