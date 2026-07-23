package com.tencent.news.core.compose.view.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.animation.core.Animatable
import com.tencent.kuikly.compose.animation.core.Spring
import com.tencent.kuikly.compose.animation.core.spring
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.input.pointer.PointerEvent
import com.tencent.kuikly.compose.ui.input.pointer.PointerEventPass
import com.tencent.kuikly.compose.ui.layout.onSizeChanged
import com.tencent.kuikly.compose.ui.unit.IntOffset
import com.tencent.kuikly.compose.ui.util.fastForEach
import com.tencent.news.core.compose.view.pointerInputFilter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 拖拽关闭的配置参数
 *
 * @param dismissThreshold 触发关闭的拖拽距离占内容高度的比例，默认 0.25（即拖拽超过内容高度的 25% 时关闭）
 * @param maxDismissThresholdPx 触发关闭的最大拖拽距离，避免内容高度异常偏大时关闭阈值过高
 * @param velocityThreshold 触发关闭的速度阈值（像素/秒），快速下滑时即使距离不够也可关闭
 * @param touchSlopPx 手势方向判定的滑动阈值（像素），手指移动超过此距离才判定方向
 */
data class DragToDismissConfig(
    val dismissThreshold: Float = 0.25f,
    val maxDismissThresholdPx: Float = 100f,
    val velocityThreshold: Float = 1000f,
    val touchSlopPx: Float = 30f
)

/**
 * 可拖拽关闭的容器组件。
 *
 * 将内容包裹在此容器中，用户可以通过向下拖拽来关闭弹窗。
 * 拖拽过程中内容会跟手向下偏移，松手后根据拖拽距离和速度决定是否关闭。
 *
 * 手势冲突处理策略（两阶段协作）：
 *
 * **Initial 阶段**（事件先到达父组件，再传递给子组件）：
 * - 如果已处于拖拽模式（`dragging = true`），立即消费所有事件并驱动偏移，
 *   子列表不会收到任何事件。这解决了"拖拽过程中子列表仍在滚动"的问题。
 * - 如果未处于拖拽模式，放行给子组件处理。
 *
 * **Final 阶段**（子组件先处理完，再回到父组件）：
 * - 仅在未进入拖拽模式时工作，用于方向判定。
 * - 检查事件是否已被子组件消费（子列表正在滚动）：
 *   - 已消费 → 子列表可以滚动，不启动拖拽
 *   - 未消费且向下滑动超过阈值 → 子列表不可滚动，启动拖拽模式
 * - 一旦设置 `dragging = true`，从下一个事件开始 Initial 阶段即全量拦截。
 *
 * @param onDismiss 拖拽关闭时的回调
 * @param config 拖拽关闭的配置参数
 * @param content 弹窗内容
 */
@Composable
fun DragToDismissContainer(
    onDismiss: () -> Unit,
    config: DragToDismissConfig = DragToDismissConfig(),
    /**
     * 可选回调：拖拽过程中实时通知当前偏移量（像素）。
     * 可用于在拖拽时同步驱动外部动画（如视频跟手缩放），消除停顿感。
     * 回弹/取消时会以 0f 回调，表示偏移归零。
     */
    onDragOffsetChange: ((Float) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentOnDragOffsetChange by rememberUpdatedState(onDragOffsetChange)
    val dragOffsetY = remember { Animatable(0f) }
    var contentHeight by remember { mutableFloatStateOf(0f) }

    // 手势追踪状态
    var lastY by remember { mutableFloatStateOf(0f) }
    var lastTime by remember { mutableStateOf(0L) }
    var downY by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    // 方向判定：null=未判定，true=向下拖拽，false=非拖拽（交给子组件）
    var directionDecided by remember { mutableStateOf<Boolean?>(null) }
    var isPointerDown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .onSizeChanged { size ->
                contentHeight = size.height.toFloat()
            }
            // ═══ Initial 阶段：拖拽模式下拦截事件 + 驱动偏移 ═══
            .pointerInputFilter(
                pass = PointerEventPass.Initial,
                onTouchEvent = { event: PointerEvent ->
                    if (!dragging) return@pointerInputFilter

                    val change = event.changes.firstOrNull() ?: return@pointerInputFilter

                    // 如果已经进入拖拽模式，但发现 change 被消费了，
                    // 说明子组件抢占了事件，终止拖拽并回弹
                    if (change.isConsumed) {
                        dragging = false
                        directionDecided = null
                        if (dragOffsetY.value > 0f) {
                            scope.launch {
                                dragOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                currentOnDragOffsetChange?.invoke(0f)
                            }
                        }
                        return@pointerInputFilter
                    }

                    // 已进入拖拽模式 → 消费所有事件，子列表收不到
                    event.changes.fastForEach { it.consume() }

                    if (change.pressed && isPointerDown) {
                        // 手指移动：驱动偏移量
                        val delta = change.position.y - lastY
                        lastY = change.position.y
                        lastTime = change.uptimeMillis
                        val newOffset = (dragOffsetY.value + delta).coerceAtLeast(0f)
                        scope.launch {
                            dragOffsetY.snapTo(newOffset)
                            currentOnDragOffsetChange?.invoke(newOffset)
                        }
                    } else if (!change.pressed && isPointerDown) {
                        // 手指抬起：判断关闭或回弹
                        isPointerDown = false
                        if (dragOffsetY.value > 0f) {
                            val dt = (change.uptimeMillis - lastTime).coerceAtLeast(1)
                            val velocityY = (change.position.y - lastY) / dt * 1000f
                            val threshold = min(
                                contentHeight * config.dismissThreshold,
                                config.maxDismissThresholdPx
                            )
                            if (dragOffsetY.value > threshold || velocityY > config.velocityThreshold) {
                                currentOnDismiss()
                            } else {
                                scope.launch {
                                    dragOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                    // 回弹完成后通知偏移归零
                                    currentOnDragOffsetChange?.invoke(0f)
                                }
                            }
                        }
                        dragging = false
                        directionDecided = null
                    }
                },
                onTouchCancel = {
                    if (dragging) {
                        isPointerDown = false
                        dragging = false
                        directionDecided = null
                        if (dragOffsetY.value > 0f) {
                            scope.launch {
                                dragOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                // 取消手势后回弹完成，通知偏移归零
                                currentOnDragOffsetChange?.invoke(0f)
                            }
                        }
                    }
                }
            )
            // ═══ Final 阶段：方向判定 + 拖拽启动 ═══
            .pointerInputFilter(
                pass = PointerEventPass.Final,
                shareWithSiblings = false,
                onTouchEvent = { event: PointerEvent ->
                    // 已进入拖拽模式时，所有逻辑都在 Initial 阶段处理，Final 不做任何事
                    if (dragging) return@pointerInputFilter

                    val change = event.changes.firstOrNull() ?: return@pointerInputFilter

                    if (change.pressed && !isPointerDown) {
                        // ── 手指按下 ──
                        isPointerDown = true
                        downY = change.position.y
                        lastY = change.position.y
                        lastTime = change.uptimeMillis
                        // 如果上次拖拽未完全回弹（偏移 > 0），直接进入拖拽模式
                        if (dragOffsetY.value > 0f) {
                            dragging = true
                            directionDecided = true
                        } else {
                            directionDecided = null
                        }
                    } else if (change.pressed && isPointerDown) {
                        // ── 手指移动（方向判定阶段） ──
                        if (directionDecided == null) {
                            val dy = change.position.y - downY
                            if (abs(dy) > config.touchSlopPx) {
                                // 判断子列表是否可以滚动（通过 change.isConsumed 判断）
                                val listCanScroll = change.isConsumed
                                if (dy > 0 && !listCanScroll) {
                                    // 向下滑动 + 子列表不可滚动 → 进入拖拽模式
                                    directionDecided = true
                                    dragging = true
                                    // 注意：dragging = true 后，从下一个事件开始
                                    // Initial 阶段将全量拦截，子列表不再收到事件
                                } else {
                                    // 向上滑动 或 子列表可滚动 → 放弃拖拽
                                    directionDecided = false
                                }
                            }
                        }
                        lastY = change.position.y
                        lastTime = change.uptimeMillis
                    } else if (!change.pressed && isPointerDown) {
                        // ── 手指抬起（未进入拖拽模式的情况） ──
                        isPointerDown = false
                        directionDecided = null
                    }
                },
                onTouchCancel = {
                    if (!dragging) {
                        isPointerDown = false
                        directionDecided = null
                    }
                }
            )
            .offset { IntOffset(0, dragOffsetY.value.roundToInt()) }
    ) {
        content()
    }
}
