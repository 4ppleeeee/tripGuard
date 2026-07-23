package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.tencent.kuikly.compose.foundation.gestures.awaitEachGesture
import com.tencent.kuikly.compose.foundation.gestures.awaitFirstDown
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.widgetbtns.DefaultTitlebarAreaHeight
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.page.model.StructListHeaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first


/**
 * VerticalPager 下拉刷新指示器浮层（覆盖模式）
 * 指示器固定覆盖在列表顶部，通过 pullProgress 控制从顶部滑入的位置，
 * 下拉时列表内容不移动，指示器从列表顶部上方滑入覆盖在列表上方
 */
@Composable
internal fun PullRefresh4VerticalPager(
    state: VerticalPagerPullRefreshState,
    foregroundColor: Color?,
) {

    // 注册下拉刷新副作用（监听刷新状态变化，触发刷新请求）
    VerticalPagerPullRefreshEffects(state)

    // 使用较小的阈值避免 pullProgress 在 0 附近微小波动时反复显隐导致闪烁
    val showIndicator = state.isRefreshing || state.pullProgress > 0.01f
    if (!showIndicator) {
        return
    }

    // 覆盖模式：指示器从 -(refreshThreshold) 滑入到 topMargin（完全露出）
    // pullProgress 0→1 对应 offsetY 从 -(refreshThreshold - topMargin) → topMargin
    // 刷新中时固定在 topMargin（完全露出）
    val topMargin = state.indicatorTopMargin
    val indicatorOffsetY = if (state.isRefreshing) {
        topMargin
    } else {
        topMargin - state.refreshThreshold * (1f - state.pullProgress)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(state.refreshThreshold)
            .offset(y = indicatorOffsetY),
        contentAlignment = Alignment.Center
    ) {
        PullRefreshHeaderView(
            pullProgress = state.pullProgress,
            isRefreshing = state.isRefreshing,
            refreshThreshold = state.refreshThreshold,
            foregroundColor = foregroundColor,
        )
    }
}

/**
 * VerticalPager 下拉刷新状态容器
 */
internal class VerticalPagerPullRefreshState(
    val pagerState: PagerState,

    val refreshThreshold: Dp = DEFAULT_REFRESH_THRESHOLD,

    /**
     * 下拉粘滞系数（值越小拖拽阻力感越强）
     * 例如 0.5f 表示手势下拉 100px 时，实际驱动的下拉动画距离为 50px。
     */
    val dragFriction: Float = 1f,

    /**
     * 指示器顶部 margin，用于避开 TitleBar 等固定区域。
     * 默认 0.dp，表示指示器紧贴列表顶部。
     */
    val indicatorTopMargin: Dp = 0.dp,
) {
    var isRefreshing by mutableStateOf(false)
    var pullProgress by mutableStateOf(0f)


    /** 判断 VerticalPager 是否在第一页（仅第一页支持下拉刷新） */
    fun isAtFirstPage(): Boolean {
        return pagerState.currentPage == 0 && pagerState.currentPageOffsetFraction <= 0f
    }
}

@Composable
internal fun rememberVerticalPagerPullRefreshState(
    pagerState: PagerState,
): VerticalPagerPullRefreshState {
    val marginTop = DefaultTitlebarAreaHeight

    val refreshState = remember(pagerState) {
        VerticalPagerPullRefreshState(
            pagerState = pagerState,
            dragFriction = 0.75f,
            indicatorTopMargin = marginTop
        )
    }

    // 监听页面切换：当用户滑到非第一页时，重置下拉刷新进度
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage ->
            if (currentPage != 0 && refreshState.pullProgress > 0f) {
                refreshState.pullProgress = 0f
            }
        }
    }

    return refreshState
}

/**
 * VerticalPager 下拉刷新手势检测 Modifier（覆盖模式）
 * 列表始终固定不动，通过 pointerInput 检测下拉手势来驱动 pullProgress，
 * 不依赖 ScrollerView 的 contentOffset 变化
 *
 * @param blockPullRefresh 返回 true 时阻止下拉刷新（如榜单 Header 存在且未折叠时，下拉应折叠榜单而非刷新）
 */
internal fun Modifier.verticalPagerPullRefreshPointerInput(
    enableHeader: Boolean,
    vpRefreshState: VerticalPagerPullRefreshState,
    blockPullRefresh: () -> Boolean = { false },
): Modifier {
    if (!enableHeader) return this
    return this.pointerInput(enableHeader) {
        // 在 PointerInputScope 中通过 Density 接口将 dp 正确转换为 px
        val refreshThresholdPx = vpRefreshState.refreshThreshold.toPx()
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = true)
            // 仅在第一页且未在刷新中时才检测下拉；榜单 Header 存在且未折叠时不触发下拉刷新
            if (!vpRefreshState.isAtFirstPage() || vpRefreshState.isRefreshing || blockPullRefresh()) return@awaitEachGesture

            var lastY = down.position.y
            var lastX = down.position.x
            var totalPullDistance = 0f
            var isDraggingDown = false
            // 角度判定：累计水平和垂直位移，用于判断滑动方向
            var cumulativeDeltaX = 0f
            var cumulativeDeltaY = 0f
            // 方向是否已确定（一旦确定就不再重新判断）
            var directionDecided = false

            while (true) {
                val event = awaitPointerEvent()
                val drag = event.changes.find { it.id == down.id } ?: break

                // 仅当 drag 事件未被消费时，才触发后续下拉刷新流程
                if (drag.isConsumed) break

                if (!drag.pressed) {
                    // 手指抬起 → 判断是否触发刷新
                    if (isDraggingDown && vpRefreshState.pullProgress >= 1f) {
                        vpRefreshState.isRefreshing = true
                    } else {
                        vpRefreshState.pullProgress = 0f
                    }
                    isDraggingDown = false
                    break
                }

                val currentY = drag.position.y
                val currentX = drag.position.x
                val dragAmountY = currentY - lastY
                val dragAmountX = currentX - lastX
                lastY = currentY
                lastX = currentX

                if (!isDraggingDown && !directionDecided && dragAmountY > 0 && vpRefreshState.isAtFirstPage()) {
                    // 累计位移用于角度判定
                    cumulativeDeltaX += dragAmountX
                    cumulativeDeltaY += dragAmountY
                    val absDeltaX = kotlin.math.abs(cumulativeDeltaX)
                    val absDeltaY = kotlin.math.abs(cumulativeDeltaY)
                    // 需要一定位移量才做方向判定，避免微小抖动误判
                    val minSlop = 5f
                    if (absDeltaX > minSlop || absDeltaY > minSlop) {
                        directionDecided = true
                        if (absDeltaY >= absDeltaX) {
                            // 滑动轨迹与垂直方向夹角 < 45°，判定为下拉刷新
                            isDraggingDown = true
                            totalPullDistance = 0f
                        }
                        // 否则认为是横划手势，不进入下拉刷新（directionDecided=true 阻止后续再判断）
                    }
                } else if (!isDraggingDown && !directionDecided && dragAmountY > 0) {
                    // 累计位移但尚未达到判定阈值
                    cumulativeDeltaX += dragAmountX
                    cumulativeDeltaY += dragAmountY
                }

                if (isDraggingDown) {
                    // 乘以粘滞系数：手势距离 × dragFriction = 实际驱动距离
                    totalPullDistance += dragAmountY * vpRefreshState.dragFriction
                    if (totalPullDistance < 0f) {
                        // 用户在下拉过程中反向上滑，下拉距离归零
                        // 解除方向锁定，让 VerticalPager 恢复正常翻页
                        totalPullDistance = 0f
                        isDraggingDown = false
                        vpRefreshState.pullProgress = 0f
                    } else {
                        vpRefreshState.pullProgress =
                            (totalPullDistance / refreshThresholdPx).coerceIn(0f, 1f)
                        // 方向锁定：下拉刷新激活后消费手势，防止 VerticalPager 同时翻页
                        drag.consume()
                    }
                }
            }
        }
    }
}

/**
 * VerticalPager 下拉刷新副作用管理（覆盖模式）
 * 监听刷新状态变化，触发刷新请求和重置进度
 */
@Composable
private fun VerticalPagerPullRefreshEffects(state: VerticalPagerPullRefreshState) {
    val pageVM = LocalStructPageViewModel.current

    // 监听 isRefreshing 变为 true → 触发刷新请求
    LaunchedEffect(state.isRefreshing) {
        if (state.isRefreshing) {
            pageVM?.refresh(
                FeedsRefreshRequest(
                    refreshForward = ListRefreshForward.TOP_REFRESH,
                    refreshAction = ListRefreshAction.PULL_DOWN
                )
            )
            pageVM?.controller?.headerState?.first {
                it == StructListHeaderState.COMPLETE ||
                        it == StructListHeaderState.ERROR ||
                        it == StructListHeaderState.WAITING_FOR_MORE ||
                        it == StructListHeaderState.NO_MORE
            }
            // 刷新完成后短暂延迟再重置进度，避免指示器闪烁
            delay(300)
            state.pullProgress = 0f
            state.isRefreshing = false
        }
    }
}
