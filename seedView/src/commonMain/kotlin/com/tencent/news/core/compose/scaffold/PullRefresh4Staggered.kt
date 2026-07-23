package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.views.IScrollerViewEventObserver
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.ScrollerAttr
import com.tencent.kuikly.core.views.ScrollerEvent
import com.tencent.kuikly.core.views.ScrollerView
import com.tencent.news.core.compose.platform.ForceNoScale
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.page.model.StructListHeaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
internal fun PullRefresh4Staggered(
    refreshState: StaggeredPullRefreshState,
    foregroundColor: Color?,
) {
    StaggeredPullRefreshEffects(refreshState)   // 下拉监听
    StaggeredPullRefreshIndicator(refreshState, foregroundColor) // 下拉UI组件
}

@Composable
internal fun rememberStaggeredPullRefreshState(
    enableHeader: Boolean,
    realListState: LazyStaggeredGridState,
): StaggeredPullRefreshState {
    return remember(enableHeader, realListState) {
        StaggeredPullRefreshState(realListState)
    }
}

/**
 * 瀑布流下拉刷新状态容器
 */
internal class StaggeredPullRefreshState(
    val realListState: LazyStaggeredGridState,
) {
    var isRefreshing by mutableStateOf(false)
    var pullProgress by mutableStateOf(0f)
    var scrollerViewRef by mutableStateOf<ScrollerView<ScrollerAttr, ScrollerEvent>?>(null)

    // 是否展示指示器内容（确认向下拉后才展示，刷新完成后隐藏）
    var showIndicator by mutableStateOf(false)

    // 用于指示器定位的原生 offsetY（dp 值），由 observer 回调更新
    var nativeContentOffsetY by mutableStateOf(0f)

    // 记录 dragBegin 时的 offsetY，用于计算下拉距离（差值）
    var dragStartOffsetY = 0f

    // 标记当前拖拽是否从顶部开始
    var isDraggingFromTop = false

    val refreshThreshold = DEFAULT_REFRESH_THRESHOLD
    val refreshThresholdDp get() = refreshThreshold.value

    /** 判断列表是否在顶部 */
    fun isAtTop(): Boolean {
        val firstIndex = realListState.firstVisibleItemIndex
        val firstOffset = realListState.firstVisibleItemScrollOffset
        return firstIndex == 0 && firstOffset <= 0
    }
}

/**
 * 瀑布流下拉刷新副作用管理
 * 包含：ScrollerView observer 注册、pullProgress 监听、刷新完成收起指示器
 */
@Composable
private fun StaggeredPullRefreshEffects(state: StaggeredPullRefreshState) {
    val coroutineScope = rememberCoroutineScope()
    val pageVM = LocalStructPageViewModel.current

    // 注册 ScrollerView 的 observer：更新 pullProgress 和在松手时判断是否触发刷新
    if (state.scrollerViewRef != null) {
        DisposableEffect(state.scrollerViewRef) {
            val scrollerView = state.scrollerViewRef ?: return@DisposableEffect onDispose { }
            scrollerView.setContentInset(top = 0f, animated = false) // 参数单位：dp
            scrollerView.setContentInsetWhenEndDrag(top = 0f) // 参数单位：dp
            val observer = object : IScrollerViewEventObserver {
                override fun onContentOffsetDidChanged(
                    contentOffsetX: Float,
                    contentOffsetY: Float,
                    params: ScrollParams
                ) {
                    state.nativeContentOffsetY = contentOffsetY

                    if (state.isDraggingFromTop && params.isDragging && state.isAtTop()) {
                        // 下拉距离 = dragStartOffsetY - 当前 offsetY（下拉时 offsetY 减小）
                        val pullDistance = state.dragStartOffsetY - contentOffsetY
                        state.pullProgress = if (pullDistance > 0) {
                            state.showIndicator = true
                            (pullDistance / state.refreshThresholdDp).coerceIn(0f, 1f)
                        } else {
                            if (!state.isRefreshing) {
                                state.showIndicator = false
                            }
                            0f
                        }
                    } else if (!params.isDragging && !state.isRefreshing) {
                        // 非拖拽且非刷新中，重置进度
                        state.pullProgress = 0f
                    }
                }

                override fun scrollerDragBegin(params: ScrollParams) {
                    state.dragStartOffsetY = params.offsetY
                    // 判断拖拽开始时是否在顶部（bounces 下拉时 firstOffset 可能为负值，也算顶部）
                    state.isDraggingFromTop = state.isAtTop()
                }

                override fun scrollerDragEnd(params: ScrollParams) {
                    if (state.isDraggingFromTop && state.pullProgress >= 1f && !state.isRefreshing) {
                        // 从顶部下拉且超过阈值且未在刷新中 → 触发刷新
                        state.isRefreshing = true
                        scrollerView.setContentInset(
                            top = state.refreshThresholdDp,
                            animated = true
                        ) // 参数单位：dp

                        coroutineScope.launch {
                            pageVM?.refresh(
                                FeedsRefreshRequest(
                                    refreshForward = ListRefreshForward.TOP_REFRESH,
                                    refreshAction = ListRefreshAction.PULL_DOWN
                                )
                            )
                            // 先等待 headerState 变为 LOADING 或 ERROR（确认请求已开始，或请求创建失败直接报错）
                            val startState = pageVM?.controller?.headerState?.first {
                                it == StructListHeaderState.LOADING ||
                                        it == StructListHeaderState.ERROR ||
                                        it == StructListHeaderState.COMPLETE ||
                                        it == StructListHeaderState.WAITING_FOR_MORE ||
                                        it == StructListHeaderState.NO_MORE
                            }
                            // 请求成功开始后，再等待刷新结束（COMPLETE 之后会立即转为 WAITING_FOR_MORE 或 NO_MORE）
                            if (startState == StructListHeaderState.LOADING) {
                                pageVM?.controller?.headerState?.first {
                                    it == StructListHeaderState.COMPLETE ||
                                            it == StructListHeaderState.ERROR ||
                                            it == StructListHeaderState.WAITING_FOR_MORE ||
                                            it == StructListHeaderState.NO_MORE
                                }
                            }
                            state.pullProgress = 0f
                            state.showIndicator = false
                            state.isRefreshing = false
                        }
                    } else if (!state.isRefreshing) {
                        scrollerView.setContentInsetWhenEndDrag(top = 0f) // 参数单位：dp
                        state.showIndicator = false
                    }
                    // 松手后重置拖拽状态
                    state.isDraggingFromTop = false
                }
            }
            scrollerView.addScrollerViewEventObserver(observer)
            onDispose {
                scrollerView.removeScrollerViewEventObserver(observer)
                scrollerView.setContentInset(top = 0f, animated = false) // 参数单位：dp
                scrollerView.setContentInsetWhenEndDrag(top = 0f) // 参数单位：dp
                state.isRefreshing = false
                state.pullProgress = 0f
                state.showIndicator = false
                state.nativeContentOffsetY = 0f
                state.dragStartOffsetY = 0f
                state.isDraggingFromTop = false
            }
        }
    }

    // 监听 pullProgress 变化，动态设置松手回弹位置（预设 inset，确保松手后停在正确位置）
    LaunchedEffect(state) {
        snapshotFlow { state.pullProgress >= 1f && !state.isRefreshing }
            .collect { shouldRetainOnRelease ->
                val scrollerView = state.scrollerViewRef ?: return@collect
                if (shouldRetainOnRelease) {
                    scrollerView.setContentInsetWhenEndDrag(top = state.refreshThresholdDp) // 参数单位：dp
                } else if (!state.isRefreshing) {
                    scrollerView.setContentInsetWhenEndDrag(top = 0f) // 参数单位：dp
                }
            }
    }

    // 监听刷新完成：收起指示器（仅在 isRefreshing 从 true → false 时触发）
    LaunchedEffect(state) {
        var wasRefreshing = false
        snapshotFlow { state.isRefreshing }.collect { isRefreshing ->
            if (wasRefreshing && !isRefreshing) {
                val scrollerView = state.scrollerViewRef ?: return@collect
                scrollerView.setContentInset(top = 0f, animated = true) // 参数单位：dp
                scrollerView.setContentInsetWhenEndDrag(top = 0f) // 参数单位：dp
                state.pullProgress = 0f
                state.showIndicator = false
                // 短暂延迟后再重置状态，避免快速连续下拉刷新时指示器闪烁
                delay(300)
            }
            wasRefreshing = isRefreshing
        }
    }
}

/**
 * 瀑布流下拉刷新指示器浮层
 * 通过 nativeContentOffsetY 控制垂直位置，setContentInset 会将列表内容下推，
 * 浮层固定在列表顶部上方，随 contentInset 露出
 */
@Composable
private fun StaggeredPullRefreshIndicator(
    state: StaggeredPullRefreshState,
    foregroundColor: Color?,
) {
    // 鸿蒙的 ScrollerView 表现与安卓不一致：
    // 当下拉刷新松手时，ScrollerView 会自动将 contentInset 设置为 0，导致浮层被隐藏
    val indicatorOffsetY = when {
        isHarmonyPlatform() && state.isRefreshing -> {
            if (state.nativeContentOffsetY < 0f) {
                (-state.refreshThreshold + state.nativeContentOffsetY.unaryMinus().dp)
                    .coerceAtLeast(0.dp)
            } else {
                -(state.nativeContentOffsetY.coerceAtMost(state.refreshThresholdDp)).dp
            }
        }

        else -> {
            -state.refreshThreshold + state.nativeContentOffsetY.coerceAtMost(0f)
                .unaryMinus().dp
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(state.refreshThreshold)
            .offset(y = indicatorOffsetY),
        contentAlignment = Alignment.Center
    ) {
        if (state.showIndicator) {
            ForceNoScale {
                PullRefreshHeaderView(
                    pullProgress = state.pullProgress,
                    isRefreshing = state.isRefreshing,
                    refreshThreshold = state.refreshThreshold,
                    foregroundColor = foregroundColor,
                )
            }
        }
    }
}
