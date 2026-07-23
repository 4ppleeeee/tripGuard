package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.tencent.kuikly.compose.foundation.gestures.awaitEachGesture
import com.tencent.kuikly.compose.foundation.gestures.awaitFirstDown
import com.tencent.kuikly.compose.foundation.gestures.scrollBy
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.pager.PageSize
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.foundation.pager.VerticalPager
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose_dsl.kuikly.extension.NestedScrollMode
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose_dsl.kuikly.extension.nestedScroll
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.isFooterLoading
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.isFooterNoMore
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.isFooterReadyToLoad
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.tryAutoBottomRefresh
import com.tencent.news.core.compose.scaffold.card.FeedsItemCtx
import com.tencent.news.core.compose.scaffold.card.IDislikeHandler
import com.tencent.news.core.compose.scaffold.card.checkCellSelected
import com.tencent.news.core.compose.scaffold.lifecycle.rememberPageLifecycleDispatcher
import com.tencent.news.core.compose.scaffold.modifiers.willAppear
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.registry.LocalStructVerticalPagerIndex
import com.tencent.news.core.compose.scaffold.registry.LocalStructVerticalPagerOffsetFraction
import com.tencent.news.core.compose.scaffold.registry.LocalStructVerticalPagerSwipeAlphaSourceIndex
import com.tencent.news.core.compose.view.list.PagerStateAdapter
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.model.VerticalPagerListConfig
import com.tencent.news.core.page.extension.StructPageWidgetEx.findAllDialogVM
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.ChannelWidget.Companion.enableHeader
import com.tencent.news.core.page.model.IVerticalPagerCellAware
import com.tencent.news.core.service.ViewService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlin.math.abs

/**
 * VerticalPager - 垂直分页滚动
 */
@Composable
internal fun StructVerticalPagerView(
    channelWidget: ChannelWidget,
    listState: PagerStateAdapter,
    displayItems: List<IKmmFeedsItem>,
    dislikeHandler: IDislikeHandler,
    verticalPagerConfig: VerticalPagerListConfig,
) {
    val page = LocalActivity.current as? ComposePage
    val pageVM = LocalStructPageViewModel.current
    val enableHeader = channelWidget.enableHeader()
    // 从listState中获取pagerState
    val pagerState = listState.realListState as PagerState

    // 使用 rememberUpdatedState 确保在 LaunchedEffect 中始终获取最新的 displayItems
    val currentDisplayItems by rememberUpdatedState(displayItems)

    // 【临时方案 / TODO】弹窗联动禁用列表滚动
    // ------------------------------------------------------------------
    // 背景：评论面板等 BottomSheet Dialog 打开后，蒙层只拦截了 tap 事件，
    //   但 VerticalPager 的竖向 drag 手势会穿透蒙层，导致背后的视频流仍可滑动切换。
    // 当前做法：扫描当前页面及父页面所有 IStructDialogVM，任一 showDialogState=true
    //   时把 userScrollEnabled 置为 false，临时规避穿透问题。
    // 局限：
    //   1. 依赖业务弹窗严格走 IStructDialogVM 协议；非标准弹窗（比如直接用
    //      DialogController 弹出的 Dialog）无法被感知。
    //   2. 每次重组都做一次 widget 树扫描（仅在 rootWidget 变化时重算），
    //      对超大 widget 树存在少量扫描开销。
    //   3. 只处理了 VerticalPager 场景；LazyColumn / LazyRow / HorizontalPager
    //      若出现同类问题，需要各自补同款逻辑。
    // 后续根因修复方向：
    //   a. 让 Kuikly Dialog 蒙层在手势层彻底吞掉 pointerInput/drag，使弹窗
    //      本身就是"事件黑洞"，列表侧无需关心；
    //   b. 或由 DialogController 维护 Page 级 LocalDialogVisible CompositionLocal，
    //      所有可滚动容器统一消费，避免按 widget 树扫描。
    // 待 a/b 任一方案落地后，应移除本段及 rememberAnyStructDialogVisible。
    val anyDialogVisible = rememberAnyStructDialogVisible(pageVM)

    // 从verticalPagerConfig中获取分页配置；并根据弹窗状态动态禁用滚动
    val userScrollEnabled = verticalPagerConfig.userScrollEnabled && !anyDialogVisible

    if (displayItems.isEmpty()) {
        // 空状态处理
        Box(modifier = Modifier.fillMaxSize())
    } else {
        val safeCurrentPage = pagerState.currentPage.coerceIn(0, displayItems.lastIndex)
        if (safeCurrentPage != pagerState.currentPage) {
            LaunchedEffect(pagerState, displayItems.size, safeCurrentPage) {
                pagerState.scrollToPage(safeCurrentPage)
            }
            Box(modifier = Modifier.fillMaxSize())
            return
        }

        // 监听页面切换，更新全局索引（用于广场item插入位置计算）
        LaunchedEffect(pagerState, displayItems.size) {
            snapshotFlow { pagerState.currentPage }.collect { currentPage ->
                verticalPagerConfig.onPageChanged?.invoke(currentPage)
                (pageVM as? IStructVerticalPagerLifecycle)?.updateCurrentVisibleIndex(currentPage)

                val preLoadMoreCount = verticalPagerConfig.preLoadMoreCount
                if (preLoadMoreCount > 0) {
                    val reachBottom = displayItems.size - currentPage - 1 <= preLoadMoreCount
                    if (reachBottom && pageVM.isFooterReadyToLoad()) {
                        pageVM.tryAutoBottomRefresh()
                    }
                }
            }
        }

        // 监听scrollStateFlow，处理滚动到指定位置的事件
        LaunchedEffect(pagerState) {
            pageVM?.scrollStateFlow?.collect { scrollState ->
                val items = currentDisplayItems
                if (scrollState.scrollToPosition >= 0 && scrollState.scrollToPosition < items.size) {
                    delay(100)
                    if (scrollState.animate) {
                        pagerState.animateScrollToPage(scrollState.scrollToPosition)
                    } else {
                        pagerState.scrollToPage(scrollState.scrollToPosition)
                    }
                }
            }
        }

        // 监听数据列表长度变化：当新数据加载后，若当前停留在旧的最后一页，
        // Pager 内部的手势状态可能仍处于边界锁定状态，需要通过 scrollToPage 唤醒，
        // 使其重新评估 canScrollForward，从而恢复向后翻页能力
        LaunchedEffect(pagerState) {
            var lastItemCount = displayItems.size
            snapshotFlow { currentDisplayItems.size }.collect { newItemCount ->
                val currentPage = pagerState.currentPage
                // 仅当数据增加，且当前页是旧列表的最后一页时才需要唤醒
                if (newItemCount > lastItemCount && currentPage == lastItemCount - 1) {
                    // 重新触发一下滑动
                    pagerState.scrollBy(-1f)
                }
                lastItemCount = newItemCount
            }
        }

        // 用于检测最后一条数据的边界滑动（累计向上滑动距离）
        var accumulatedUpDrag by remember { mutableStateOf(0f) }
        // 边界滑动检测阈值（像素），用户向上滑动超过此距离触发提示
        val boundaryScrollThreshold = 50f
        // 用于控制最后一条卡片向下滑动时是否允许滚动
        var blockDownScroll by remember { mutableStateOf(false) }

        // 监听当前页面变化，当在最后一页时阻止向下滚动
        LaunchedEffect(pagerState, displayItems.size) {
            snapshotFlow { pagerState.currentPage }.collect { currentPage ->
                blockDownScroll = currentPage == displayItems.size - 1
            }
        }

        val pagerIndexState = remember { derivedStateOf { pagerState.currentPage } }
        val pagerOffsetFractionState = remember {
            derivedStateOf { pagerState.currentPageOffsetFraction }
        }
        var swipeAlphaSourceIndex by remember { mutableStateOf(NO_SWIPE_ALPHA_SOURCE_INDEX) }
        val swipeAlphaSourceIndexState = remember {
            derivedStateOf { swipeAlphaSourceIndex }
        }

        // 下拉刷新状态（仅在 enableHeader 且当前在第一页时有效）
        val vpRefreshState = rememberVerticalPagerPullRefreshState(pagerState)
        val pullRefreshHeaderForegroundColor = channelWidget.status.getPullRefreshHeaderForegroundColor()

        // 临时规避：安卓上，打开回弹时，Pager下拉手势有bug；临时处理，选中首个时强行禁用
        val effectiveBouncesEnable by remember(displayItems.size) {
            derivedStateOf {
                if (displayItems.size > 1 && pagerState.currentPage == 0) {
                    false
                } else {
                    verticalPagerConfig.enableBounce
                }
            }
        }

        // 覆盖模式：列表始终固定不动，下拉手势仅控制 header 指示器滑出
        // 禁用 bounces，使用 SELF_ONLY 嵌套滚动，列表不响应下拉位移
        val pagerModifier = Modifier
            .fillMaxSize()
            .willAppear { page?.onPageFirstFrameRendered() }
            .nestedScroll(NestedScrollMode.SELF_ONLY, NestedScrollMode.SELF_ONLY)
            .bouncesEnable(effectiveBouncesEnable)

        CompositionLocalProvider(
            LocalStructVerticalPagerIndex provides pagerIndexState,
            LocalStructVerticalPagerOffsetFraction provides pagerOffsetFractionState,
            LocalStructVerticalPagerSwipeAlphaSourceIndex provides swipeAlphaSourceIndexState,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                VerticalPager(
                    state = pagerState,
                    modifier = pagerModifier
                        .verticalPagerPullRefreshPointerInput(
                            enableHeader = enableHeader,
                            vpRefreshState = vpRefreshState,
                            blockPullRefresh = { currentDisplayItems.firstOrNull()?.blockPullRefresh == true },
                        )
                        .pointerInput(userScrollEnabled, pagerState) {
                            try {
                                if (!userScrollEnabled) {
                                    swipeAlphaSourceIndex = NO_SWIPE_ALPHA_SOURCE_INDEX
                                    return@pointerInput
                                }
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    val startPage = pagerState.currentPage
                                    var lastX = down.position.x
                                    var lastY = down.position.y
                                    var totalX = 0f
                                    var totalY = 0f
                                    var trackingSourceIndex = NO_SWIPE_ALPHA_SOURCE_INDEX

                                    try {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val drag = event.changes.find { it.id == down.id } ?: break
                                            if (!drag.pressed) break

                                            val currentX = drag.position.x
                                            val currentY = drag.position.y
                                            totalX += currentX - lastX
                                            totalY += currentY - lastY
                                            lastX = currentX
                                            lastY = currentY

                                            if (trackingSourceIndex == NO_SWIPE_ALPHA_SOURCE_INDEX) {
                                                val absX = abs(totalX)
                                                val absY = abs(totalY)
                                                val hasTargetPage = when {
                                                    totalY > 0f -> startPage > 0
                                                    totalY < 0f -> startPage < currentDisplayItems.lastIndex
                                                    else -> false
                                                }
                                                if (hasTargetPage &&
                                                    absY > VERTICAL_PAGER_SWIPE_ALPHA_DRAG_SLOP &&
                                                    absY >= absX
                                                ) {
                                                    trackingSourceIndex = startPage
                                                    swipeAlphaSourceIndex = startPage
                                                }
                                            }

                                            if (trackingSourceIndex != NO_SWIPE_ALPHA_SOURCE_INDEX &&
                                                pagerState.currentPage != startPage
                                            ) {
                                                break
                                            }
                                        }
                                    } finally {
                                        if (trackingSourceIndex != NO_SWIPE_ALPHA_SOURCE_INDEX &&
                                            swipeAlphaSourceIndex == trackingSourceIndex
                                        ) {
                                            swipeAlphaSourceIndex = NO_SWIPE_ALPHA_SOURCE_INDEX
                                        }
                                    }
                                }
                            } finally {
                                swipeAlphaSourceIndex = NO_SWIPE_ALPHA_SOURCE_INDEX
                            }
                        }
                        .pointerInput(blockDownScroll, displayItems.size) {
                            // 当在最后一页时，监听向上滑动手势（不消费手势，只做检测）
                            if (blockDownScroll) {
                                awaitEachGesture {
                                    // 等待按下事件（不消费，允许穿透）
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    accumulatedUpDrag = 0f
                                    var lastY = down.position.y

                                    // 追踪拖动
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val drag = event.changes.find { it.id == down.id } ?: break

                                        if (!drag.pressed) {
                                            // 手指抬起
                                            break
                                        }

                                        val currentY = drag.position.y
                                        val dragAmount = currentY - lastY
                                        lastY = currentY

                                        // dragAmount < 0 表示向上滑动（手指向上移动）
                                        if (dragAmount < 0) {
                                            accumulatedUpDrag += dragAmount
                                        }
                                        // 不消费手势，让VerticalPager可以正常处理滑动
                                    }

                                    // 拖动结束，检查是否触发边界滑动
                                    if (accumulatedUpDrag < -boundaryScrollThreshold) {
                                        if (pageVM.isFooterNoMore()) {
                                            // 已到末页，用户在最后一页向上滑动超过阈值，展示末页提示
                                            (pageVM as? IStructVerticalPagerLifecycle)?.onBoundaryScrollAttempt()
                                        } else if (!pageVM.isFooterLoading()) {
                                            // 由于加载失败等操作，用户可能已经翻到底了；
                                            // 此时 currentPage 不再变化，需要在这里补发一次重试
                                            pageVM.tryAutoBottomRefresh()
                                        }
                                    }
                                    accumulatedUpDrag = 0f
                                }
                            }
                        },
                    pageSize = PageSize.Fill,
                    beyondViewportPageCount = 1,
                    pageSpacing = 0.dp,
                    userScrollEnabled = userScrollEnabled,
                    key = { displayItems[it].flexDto.idStr },
                ) { index ->
                    val feedsItem = displayItems[index]

                    // 为每个 page 注入独立的生命周期流，确保页面切换时非选中 page 收到 ON_PAUSE，
                    // 视频播放器监听到 ON_PAUSE 后立即停止播放，避免多个视频同时出声
                    val childLifecycleFlow = rememberPageLifecycleDispatcher(
                        isSelected = pagerState.currentPage == index
                    )
                    CompositionLocalProvider(
                        LocalComposePageLifecycleFlow provides childLifecycleFlow
                    ) {
                        val ctx = rememberFeedsItemCtx(
                            index,
                            channelWidget.status.pagerIndex,
                            displayItems.size,
                            dislikeHandler
                        )
                        RenderVerticalPagerCell(feedsItem, ctx)
                    }
                }

                // 下拉刷新指示器浮层
                if (enableHeader) {
                    PullRefresh4VerticalPager(
                        state = vpRefreshState,
                        foregroundColor = pullRefreshHeaderForegroundColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderVerticalPagerCell(feedsItem: IListItem, feedsItemCtx: FeedsItemCtx) {
    val vm = feedsItem.asItemVM
    if (vm is IVerticalPagerCellAware) {
        val isSelected = feedsItemCtx.checkCellSelected()
        // key 同时包含 vm 实例：当 cell 在同一 slot 内被替换为新的 vm 实例（典型场景：
        // 首屏占位 cell 被真实数据 cell 替换），即使 isSelected 仍为 true，
        // 也需要为新的 vm 重新派发一次 onPagerCellSelected，避免新 vm 错过首屏选中通知。
        // 选中后延迟派发，只有 cell 稳定停留一段时间才通知；若期间滑走或 vm 替换，
        // LaunchedEffect 会被取消。
        LaunchedEffect(vm, isSelected) {
            if (isSelected) {
                vm.onPagerCellSelected()
            }

            if (isSelected) {
                delay(PAGER_CELL_SELECTED_STABLE_DELAY_MS)
                if (isSelected) {
                    vm.onPagerCellStableSelected()
                }
            }
        }
    }

    ViewService.itemCard.Build(feedsItem, feedsItemCtx)
}

private const val PAGER_CELL_SELECTED_STABLE_DELAY_MS = 1000L


/**
 * 收集当前页面及父页面所有 [com.tencent.news.core.page.vm.IStructDialogVM] 的显隐状态，
 * 任一 Dialog 显示时返回 true。
 *
 * 用于让 VerticalPager 在评论面板 / 分享面板 / 选集面板等页面级浮层打开时，
 * 自动禁用用户滚动手势，避免弹窗背后的视频流仍可竖滑切换。
 *
 * TODO(临时方案)：这是针对"Kuikly Dialog 蒙层无法完全拦截背后 drag 手势"的临时兜底。
 *   一旦 Dialog 层自身能拦截所有穿透事件，或框架提供 Page 级 LocalDialogVisible
 *   能力，应删除本方法并回归标准 userScrollEnabled 配置。详细说明见 [StructVerticalPagerView]。
 */
@Composable
fun rememberAnyStructDialogVisible(pageVM: IStructPageViewModel?): Boolean {
    return rememberAnyStructDialogVisibleState(pageVM).collectAsState(initial = false).value
}

@Composable
fun rememberAnyStructDialogVisibleState(pageVM: IStructPageViewModel?): Flow<Boolean> {
    if (pageVM == null) return flowOf(false)
    val rootWidget = pageVM.pageRootWidget
    // key 使用 rootWidget，pageWidget 重建（refresh/换源）时重新收集 dialog VM 列表
    val dialogVMs = remember(rootWidget) { rootWidget.findAllDialogVM() }
    if (dialogVMs.isEmpty()) return flowOf(false)

    val combinedFlow = remember(dialogVMs) {
        combine(dialogVMs.map { it.showDialogState }) { states ->
            states.any { it }
        }
    }
    return combinedFlow
}

private const val NO_SWIPE_ALPHA_SOURCE_INDEX = -1
private const val VERTICAL_PAGER_SWIPE_ALPHA_DRAG_SLOP = 5f
