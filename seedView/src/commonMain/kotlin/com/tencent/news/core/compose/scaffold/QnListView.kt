package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.grid.GridCells
import com.tencent.kuikly.compose.foundation.lazy.grid.GridItemSpan
import com.tencent.kuikly.compose.foundation.lazy.grid.LazyVerticalGrid
import com.tencent.kuikly.compose.foundation.lazy.grid.itemsIndexed
import com.tencent.kuikly.compose.foundation.lazy.grid.rememberLazyGridState
import com.tencent.kuikly.compose.foundation.lazy.itemsIndexed
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.itemsIndexed
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose_dsl.kuikly.Views.rememberPullToRefreshState
import com.tencent.kuikly.compose_dsl.kuikly.extension.NestedScrollMode
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.kuikly.compose_dsl.kuikly.extension.nestedScroll
import com.tencent.kuikly.core.views.IScrollerViewEventObserver
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.ScrollerAttr
import com.tencent.kuikly.core.views.ScrollerEvent
import com.tencent.kuikly.core.views.ScrollerView
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.card.FeedsItemCtx
import com.tencent.news.core.compose.scaffold.card.IDislikeHandler
import com.tencent.news.core.compose.scaffold.modifiers.willAppear
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.registry.LocalStructRootListState
import com.tencent.news.core.compose.scaffold.registry.LocalStructSelectedListState
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
import com.tencent.news.core.compose.platform.ForceNoScale
import com.tencent.news.core.compose.platform.fdp
import com.tencent.news.core.compose.utils.WindowSizeType
import com.tencent.news.core.compose.utils.WindowSizeUtils
import com.tencent.news.core.compose.view.QnLottie
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.SpacerHeight
import com.tencent.news.core.compose.view.list.IQnListState
import com.tencent.news.core.compose.view.list.LazyGridStateAdapter
import com.tencent.news.core.compose.view.list.LazyListStateAdapter
import com.tencent.news.core.compose.view.list.LazyStaggeredGridStateAdapter
import com.tencent.news.core.compose.view.list.PagerStateAdapter
import com.tencent.news.core.compose.view.list.isScrolledToBottom
import com.tencent.news.core.compose.view.list.isScrolledToTop
import com.tencent.news.core.extension.takeIfNotBlank
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.model.GridListConfig
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.model.NormalListConfig
import com.tencent.news.core.list.model.QnListConfig
import com.tencent.news.core.list.model.StaggeredGridListConfig
import com.tencent.news.core.list.model.VerticalPagerListConfig
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.ChannelWidget.Companion.enableFooter
import com.tencent.news.core.page.model.ChannelWidget.Companion.enableHeader
import com.tencent.news.core.page.model.ChannelWidgetStatus
import com.tencent.news.core.page.model.PullRefreshHeaderForegroundColorMode
import com.tencent.news.core.page.model.StructListFooterState
import com.tencent.news.core.page.model.StructListHeaderState
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.platform.FrameworkLottie
import com.tencent.news.core.service.ViewService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.tencent.news.core.compose.utils.ComposeUtils

private const val TAG = "QnListView"
private const val LIST_LOAD_MORE_THRESHOLD = 2    // 列表布局提前2个item触发加载更多
private const val GRID_LOAD_MORE_THRESHOLD = 4    // 网格和瀑布流布局提前4个item触发加载更多
private const val TOP_LOAD_MORE_THRESHOLD = 2     // 顶部加载更多阈值：滚动到前2个item时触发（前2条件）
private const val TOP_INSERT_SCROLL_RESTORE_DELAY_MS = 16L
private const val GRID_PULL_REFRESH_RESET_DELAY_MS = 300L
private const val DEFAULT_LOAD_FINISH_FOOTER_HEIGHT_DP = 48
private const val DEFAULT_LOAD_FINISH_FOOTER_TEXT_FONT_SIZE_SP = 14
private const val LOAD_FINISH_FOOTER_TEXT_COLOR_LEVEL_T4 = 4

/**
 * 通用列表组件：支持顶部auto_more加载、底部footer加载
 */
@Composable
internal fun QnListView(
    channelWidget: ChannelWidget,
    displayItems: List<IListItem>,
    selectedListState: MutableState<IQnListState?>,
) {
    // 从原始数据源移除成本较高，先使用临时过滤的方案实现，后续改造
    var dislikedItemIds by remember { mutableStateOf(setOf<String>()) }
    val finalDisplayItems = remember(displayItems, dislikedItemIds) {
        displayItems.filter { item ->
            !dislikedItemIds.contains(item.flexDto.idStr)
        }
    }
    // 创建负反馈处理器
    val dislikeHandler = remember(dislikedItemIds) {
        createDislikeHandler { itemId ->
            if (!dislikedItemIds.contains(itemId)) {
                dislikedItemIds = dislikedItemIds + itemId
                NewsChannelLog.fileLog("Dislike", "UI层负反馈移除item: $itemId")
            }
        }
    }

    // 监听顶部刷新。sub PullRefresh 开启时由业务 hook 接管顶部加载，避免原生 topMore 抢先消费手势。
    if (!subListPullRefreshHookEnabled()) {
        key(displayItems) { // 列表数据刷新后，top状态可能会变，需重新监听
            WatchTopRefresh()
        }
    }

    val listConfig = channelWidget.status.getListConfig()
    channelWidget.enableFooter()
    channelWidget.enableHeader()
    val bottomSpace = buildListBottomSpace()

    // 所有列表类型统一创建 listState
    // 当开启滚动位置恢复时，优先使用保存的滚动位置
    val enableRestore = channelWidget.status.enableScrollPositionRestore
    val initIndexState by channelWidget.status.initIndex.collectAsState()
    val initIndex = if (enableRestore) {
        channelWidget.status.userScrolledIndex ?: initIndexState.takeIf { it > 0 } ?: 0
    } else {
        channelWidget.status.initIndex.value.takeIf { it > 0 } ?: 0
    }
    val initOffset = if (enableRestore) {
        channelWidget.status.userScrolledOffset ?: 0
    } else {
        0
    }
    val itemCount = finalDisplayItems.size
    val listState = listConfig.createListState(initIndex, initOffset, itemCount)
    if (enableRestore) {
        // 持续同步滚动位置到 ChannelWidgetStatus 基础字段
        LaunchedEffect(listState) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                channelWidget.status.userScrolledIndex = index
                channelWidget.status.userScrolledOffset = offset
            }
        }
    }
    val isSelected by channelWidget.status.isSelected.collectAsState()
    if (isSelected) {
        if (selectedListState.value != listState) {
            selectedListState.value = listState
        }
    }

    LaunchedEffect(initIndexState) {
        if (listState.firstVisibleItemIndex != initIndexState) {
            listState.scrollToItem(initIndexState)
        }
    }

    when (listState) {
        is LazyListStateAdapter -> StructListView(
            channelWidget = channelWidget,
            listState = listState,
            displayItems = finalDisplayItems,
            dislikeHandler = dislikeHandler,
            bottomSpace = bottomSpace,
            listConfig = listConfig as NormalListConfig,
        )

        is LazyGridStateAdapter -> StructGridView(
            channelWidget = channelWidget,
            listState = listState,
            displayItems = finalDisplayItems,
            dislikeHandler = dislikeHandler,
            bottomSpace = bottomSpace,
            gridConfig = listConfig as GridListConfig,
        )

        is LazyStaggeredGridStateAdapter -> StructStaggeredGridView(
            channelWidget = channelWidget,
            listState = listState,
            displayItems = finalDisplayItems,
            dislikeHandler = dislikeHandler,
            bottomSpace = bottomSpace,
            gridConfig = listConfig as StaggeredGridListConfig,
        )

        is PagerStateAdapter -> StructVerticalPagerView(
            channelWidget = channelWidget,
            listState = listState,
            displayItems = finalDisplayItems,
            dislikeHandler = dislikeHandler,
            verticalPagerConfig = listConfig as VerticalPagerListConfig,
        )

        else -> {
            // 其他未知类型，不渲染
        }
    }
}

@Composable
private fun QnListConfig.createListState(
    initIndex: Int,
    initOffset: Int,
    itemCount: Int
): IQnListState {
    return when (this) {
        is NormalListConfig -> key(this) {
            val listState = rememberLazyListState(initIndex, initOffset)
            remember { LazyListStateAdapter(listState) }
        }

        is GridListConfig -> key(this) {
            val listState = rememberLazyGridState(initIndex, initOffset)
            remember { LazyGridStateAdapter(listState) }
        }

        is StaggeredGridListConfig -> key(this) {
            val listState = rememberLazyStaggeredGridState(initIndex, initOffset)
            remember { LazyStaggeredGridStateAdapter(listState) }
        }

        is VerticalPagerListConfig -> key(this) {
            val safeInitIndex = if (itemCount > 0) initIndex.coerceIn(0, itemCount - 1) else 0
            val currentItemCount by rememberUpdatedState(itemCount)
            val pagerState = rememberPagerState(
                initialPage = safeInitIndex,
                pageCount = { currentItemCount.coerceAtLeast(1) } // 使用响应式的currentItemCount
            )
            remember { PagerStateAdapter(pagerState) }
        }
    }
}

@Composable
private fun ChannelWidgetStatus.getListConfig(): QnListConfig {
    return when (WindowSizeUtils.getWindowSizeType()) {
        WindowSizeType.SUPER_BIG -> superBigWindowListConfig
        WindowSizeType.BIG -> bigWindowListConfig
        WindowSizeType.NORMAL -> normalListConfig
    }
}

@Composable
internal fun ChannelWidgetStatus.getPullRefreshHeaderForegroundColor(): Color? {
    return when (pullRefreshHeaderForegroundColorMode) {
        PullRefreshHeaderForegroundColorMode.DEFAULT -> null
        PullRefreshHeaderForegroundColorMode.DARK_ON_LIGHT_IN_DAY -> {
            if (isAppInDarkTheme()) QnColor.t4 else QnColor.t2
        }
    }
}

@Composable
private fun buildListBottomSpace(): Float {
    val config = LocalStructPageViewModel.current?.pageRootWidget?.pageConfig
        ?: return 0f
    val bottomSafeArea = if (config.expandBottomSafeAreaForList) {
        ComposeUtils.rememberSafeAreaBottomHeight()
    } else {
        0f
    }
    return config.mainContentBottomPadding + bottomSafeArea
}

/**
 * 创建负反馈处理器
 */
private fun createDislikeHandler(
    onDislike: (String) -> Unit
): IDislikeHandler {
    return object : IDislikeHandler {
        override fun onDisLike(item: IListItem) {
            onDislike(item.flexDto.idStr)
        }
    }
}

/**
 * 创建FeedsItemCtx
 */
@Composable
internal fun rememberFeedsItemCtx(
    index: Int,
    pagerIndex: Int,
    displayItemsSize: Int,
    dislikeHandler: IDislikeHandler,
): FeedsItemCtx {
    return remember(index, pagerIndex, displayItemsSize, dislikeHandler) {
        FeedsItemCtx(
            indexInList = index,
            pagerIndex = pagerIndex,
            listSize = displayItemsSize,
            dislikeHandler = dislikeHandler,
        )
    }
}

/**
 * 渲染单个Feed项目
 */
@Composable
private fun RenderFeedsItem(
    feedsItem: IListItem,
    listCtx: FeedsItemCtx
) {
    ViewService.itemCard.Build(feedsItem, listCtx)
}


@Composable
private fun getListModifier(enableHeader: Boolean = false): Modifier {
    val page = LocalActivity.current as? ComposePage
    val up = if (enableHeader) NestedScrollMode.SELF_FIRST else NestedScrollMode.PARENT_FIRST
    return Modifier
        .fillMaxSize()
        .willAppear { page?.onPageFirstFrameRendered() }
        .nestedScroll(up, NestedScrollMode.SELF_FIRST)
        .bouncesEnable(enableHeader)  // 启用bounces以支持下拉刷新
}

/**
 * sub PullRefresh 场景下，下拉手势必须由 sub 列表独占，避免 parent root header 先展开。
 */
@Composable
private fun getSubPullRefreshListModifier(): Modifier {
    val page = LocalActivity.current as? ComposePage
    return Modifier
        .fillMaxSize()
        .willAppear { page?.onPageFirstFrameRendered() }
        .nestedScroll(NestedScrollMode.SELF_FIRST, NestedScrollMode.SELF_ONLY)
        .bouncesEnable(true)
}

/**
 * LazyColumn
 */
@Composable
private fun StructListView(
    channelWidget: ChannelWidget,
    listState: LazyListStateAdapter,
    displayItems: List<IListItem>,
    dislikeHandler: IDislikeHandler,
    bottomSpace: Float,
    listConfig: NormalListConfig,
) {
    val enableHeader = channelWidget.enableHeader()
    val enableFooter = channelWidget.enableFooter()
    val realListState = listState.realListState
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState(isRefreshing)

    val topPadding = listConfig.topPadding
    val coroutineScope = rememberCoroutineScope()
    val pageVM = LocalStructPageViewModel.current
    val pullRefreshHeaderForegroundColor =
        channelWidget.status.getPullRefreshHeaderForegroundColor()

    LazyColumn(
        state = realListState,
        beyondBoundsItemCount = 6, // 预加载个数(个数太少快速滑动白屏)
        modifier = getListModifier(enableHeader),
        contentPadding = remember(topPadding) {
            PaddingValues(top = topPadding.dp)
        }
    ) {
        if (enableHeader) {
            PullRefresh4List(
                pullToRefreshState = pullToRefreshState,
                scrollState = realListState,
                coroutineScope = coroutineScope,
                pageVM = pageVM,
                onRefreshingChanged = { refreshing ->
                    isRefreshing = refreshing
                },
                foregroundColor = pullRefreshHeaderForegroundColor,
            )
        }

        itemsIndexed(
            items = displayItems,
            key = { _, item -> item.flexDto.idStr },
            contentType = { _, item -> item.flexDto.picShowType }
        ) { index, feedsItem ->
            val ctx = rememberFeedsItemCtx(
                index, channelWidget.status.pagerIndex, displayItems.size, dislikeHandler
            )
            RenderFeedsItem(feedsItem, ctx)
        }

        // 添加Footer
        if (enableFooter) {
            item {
                StructLoadingFooterView(listState, LIST_LOAD_MORE_THRESHOLD)
            }
        }

        // 支持底部安全区，更符合iPhone X 沉浸式滚动的设计
        if (bottomSpace > 0) {
            item {
                SpacerHeight(bottomSpace.fdp)
            }
        }
    }
}

/**
 * LazyVerticalGrid - 标准网格布局
 */
@Composable
private fun StructGridView(
    channelWidget: ChannelWidget,
    listState: LazyGridStateAdapter,
    displayItems: List<IListItem>,
    dislikeHandler: IDislikeHandler,
    bottomSpace: Float,
    gridConfig: GridListConfig,
) {
    val enableFooter = channelWidget.enableFooter()
    val spanSize by remember(gridConfig) { mutableStateOf(gridConfig.gridSpanSize) }
    val realListState = listState.realListState

    // sub PullRefresh 只由业务 hook 显式开启，用于 anchor 模式下 WORKS 子列表独立加载 prev。
    // root PullRefresh 仍走原有 enableHeader 链路，避免两层下拉互相抢手势。
    val subPullRefreshEnabled = subListPullRefreshHookEnabled()
    val pullRefreshHeaderForegroundColor =
        channelWidget.status.getPullRefreshHeaderForegroundColor()
    val subRefreshState = rememberGridPullRefreshState(
        subPullRefreshEnabled,
        realListState,
    )

    val gridModifier = if (subPullRefreshEnabled) {
        getSubPullRefreshListModifier().nativeRef { _ ->
            @Suppress("UNCHECKED_CAST")
            val scrollerView = this as? ScrollerView<ScrollerAttr, ScrollerEvent>
            if (scrollerView != null) {
                subRefreshState.scrollerViewRef = scrollerView
            }
        }
    } else {
        getListModifier()
    }

    if (subPullRefreshEnabled) {
        GridPullRefreshEffects(subRefreshState, displayItems.size)
    }

    Box {
        LazyVerticalGrid(
            columns = GridCells.Fixed(spanSize),
            state = realListState,
            modifier = gridModifier,
            contentPadding = PaddingValues( // 整个列表的边距
                horizontal = gridConfig.gridHorizontalSpace.dp,
                vertical = gridConfig.gridVerticalSpace.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(
                gridConfig.itemHorizontalSpace.dp   // item之间的横向间距
            ),
            verticalArrangement = Arrangement.spacedBy(
                gridConfig.itemVerticalSpace.dp     // item之间的纵向间距
            ),
        ) {
            itemsIndexed(
                items = displayItems,
                key = { _, item -> item.flexDto.idStr },
                span = { _, item ->
                    if (item.flexDto.fullWidthInGrid) {
                        GridItemSpan(spanSize)
                    } else {
                        val itemSpanSize = gridConfig.gridSpanConfig?.get(item.flexDto.picShowType)
                            ?: gridConfig.defaultSpanSize
                        GridItemSpan(itemSpanSize)
                    }
                },
                contentType = { _, item -> item.flexDto.picShowType }
            ) { index, feedsItem ->
                val ctx = rememberFeedsItemCtx(
                    index, channelWidget.status.pagerIndex, displayItems.size, dislikeHandler
                )
                RenderFeedsItem(feedsItem, ctx)
            }

            // 添加Footer
            if (enableFooter) {
                item(span = { GridItemSpan(spanSize) }) {
                    StructLoadingFooterView(listState, GRID_LOAD_MORE_THRESHOLD)
                }
            }

            // 支持底部安全区，更符合iPhone X 沉浸式滚动的设计
            if (bottomSpace > 0) {
                item(span = { GridItemSpan(spanSize) }) {
                    SpacerHeight(bottomSpace.fdp)
                }
            }
        }

        if (subPullRefreshEnabled) {
            GridPullRefreshIndicator(
                state = subRefreshState,
                foregroundColor = pullRefreshHeaderForegroundColor,
            )
        }
    }
}

/**
 * 判断当前 PageVM 链路上是否有已开启的 sub PullRefresh hook。
 * 有 hook 时子列表接管下拉；没有则保持原生列表行为。
 */
@Composable
private fun subListPullRefreshHookEnabled(): Boolean {
    val pageVM = LocalStructPageViewModel.current ?: return false
    val rootPageVM = (pageVM.controller.rootWidget as? StructPageWidget2)
        ?.parentRootWidget?.asWidgetVM
    val hook = pageVM.subListPullRefreshHook
        ?: rootPageVM?.subListPullRefreshHook
        ?: return false
    val enabled by hook.enabled.collectAsState()
    return enabled
}

/**
 * 获取当前应消费下拉事件的 sub PullRefresh hook。
 */
@Composable
private fun currentSubListPullRefreshHook(): SubListPullRefreshHook? {
    val pageVM = LocalStructPageViewModel.current ?: return null
    val rootPageVM = (pageVM.controller.rootWidget as? StructPageWidget2)
        ?.parentRootWidget?.asWidgetVM
    return pageVM.subListPullRefreshHook ?: rootPageVM?.subListPullRefreshHook
}

private val GRID_REFRESH_THRESHOLD = 94.dp // 与 staggered 对齐

/**
 * Grid 下拉刷新状态容器（结构对齐 [StaggeredPullRefreshState]，仅 listState 类型不同）。
 */
private class GridPullRefreshState(
    val realListState: com.tencent.kuikly.compose.foundation.lazy.grid.LazyGridState,
) {
    var isRefreshing by mutableStateOf(false)
    var pullProgress by mutableStateOf(0f)
    var scrollerViewRef by mutableStateOf<ScrollerView<ScrollerAttr, ScrollerEvent>?>(null)

    var nativeContentOffsetY by mutableStateOf(0f)
    var dragStartOffsetY = 0f
    var isDraggingFromTop = false
    var skipNextRefreshEndAnimation = false

    val refreshThreshold = GRID_REFRESH_THRESHOLD
    val refreshThresholdDp get() = refreshThreshold.value

    fun isAtTop(): Boolean {
        val firstIndex = realListState.firstVisibleItemIndex
        val firstOffset = realListState.firstVisibleItemScrollOffset
        return firstIndex == 0 && firstOffset <= 0
    }
}

@Composable
private fun rememberGridPullRefreshState(
    enableHeader: Boolean,
    realListState: com.tencent.kuikly.compose.foundation.lazy.grid.LazyGridState,
): GridPullRefreshState {
    return remember(enableHeader, realListState) {
        GridPullRefreshState(realListState)
    }
}

/**
 * Grid 下拉刷新副作用：下拉后调用业务 hook 加载 prev，不触发 root page refresh。
 * 前插成功后恢复到原首屏 item，最后统一收起 native inset 和指示器。
 */
@Composable
private fun GridPullRefreshEffects(
    state: GridPullRefreshState,
    displayItemCount: Int,
) {
    val coroutineScope = rememberCoroutineScope()
    val hook = currentSubListPullRefreshHook()
    val currentDisplayItemCount by rememberUpdatedState(displayItemCount)
    val currentHook by rememberUpdatedState(hook)

    if (state.scrollerViewRef != null) {
        DisposableEffect(state.scrollerViewRef) {
            val scrollerView = state.scrollerViewRef ?: return@DisposableEffect onDispose { }
            scrollerView.setContentInset(top = 0f, animated = false)
            scrollerView.setContentInsetWhenEndDrag(top = 0f)
            val observer = object : IScrollerViewEventObserver {
                override fun onContentOffsetDidChanged(
                    offsetX: Float,
                    offsetY: Float,
                    params: ScrollParams,
                ) {
                    state.nativeContentOffsetY = offsetY
                    if (state.isDraggingFromTop && params.isDragging && state.isAtTop()) {
                        val pullDistance = state.dragStartOffsetY - offsetY
                        state.pullProgress = if (pullDistance > 0) {
                            (pullDistance / state.refreshThresholdDp).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                    } else if (!params.isDragging && !state.isRefreshing) {
                        state.pullProgress = 0f
                    }
                }

                override fun scrollerDragBegin(params: ScrollParams) {
                    state.dragStartOffsetY = params.offsetY
                    state.isDraggingFromTop = state.isAtTop()
                }

                override fun scrollerDragEnd(params: ScrollParams) {
                    if (state.isDraggingFromTop && state.pullProgress >= 1f && !state.isRefreshing) {
                        state.isRefreshing = true
                        scrollerView.setContentInset(
                            top = state.refreshThresholdDp,
                            animated = true,
                        )
                        coroutineScope.launch {
                            try {
                                val beforeFirstIndex = state.realListState.firstVisibleItemIndex
                                val beforeFirstOffset =
                                    state.realListState.firstVisibleItemScrollOffset
                                println(
                                    "[GridPullRefresh] refresh start: " +
                                            "beforeItemCount=$currentDisplayItemCount, " +
                                            "beforeFirstIndex=$beforeFirstIndex, " +
                                            "beforeFirstOffset=$beforeFirstOffset, " +
                                            "nativeOffsetY=${state.nativeContentOffsetY}, " +
                                            "pullProgress=${state.pullProgress}"
                                )
                                val refreshResult = currentHook
                                    ?.onRefreshWithScrollResult(currentDisplayItemCount)
                                val insertedCount = refreshResult?.insertedCount ?: 0
                                println(
                                    "[GridPullRefresh] refresh result: " +
                                            "beforeItemCount=$currentDisplayItemCount, " +
                                            "insertedCount=$insertedCount, " +
                                            "hasMoreTop=${refreshResult?.hasMoreTop}, " +
                                            "beforeFirstIndex=$beforeFirstIndex, " +
                                            "beforeFirstOffset=$beforeFirstOffset, " +
                                            "currentFirstIndex=${state.realListState.firstVisibleItemIndex}, " +
                                            "currentFirstOffset=${state.realListState.firstVisibleItemScrollOffset}"
                                )
                                if (insertedCount > 0) {
                                    delay(TOP_INSERT_SCROLL_RESTORE_DELAY_MS)
                                    val targetIndex = beforeFirstIndex + insertedCount
                                    state.realListState.scrollToItem(targetIndex, beforeFirstOffset)
                                    println(
                                        "[GridPullRefresh] restore after prepend: " +
                                                "targetIndex=$targetIndex, " +
                                                "targetOffset=$beforeFirstOffset, " +
                                                "restoredFirstIndex=${state.realListState.firstVisibleItemIndex}, " +
                                                "restoredFirstOffset=${state.realListState.firstVisibleItemScrollOffset}, " +
                                                "hasMoreTop=${refreshResult?.hasMoreTop}"
                                    )
                                }
                                finishGridPullRefreshAtTop(state, scrollerView)
                                delay(GRID_PULL_REFRESH_RESET_DELAY_MS)
                                if (!state.isRefreshing) {
                                    resetGridPullRefreshInset(state, scrollerView)
                                }
                                println(
                                    "[GridPullRefresh] finish refresh: " +
                                            "insertedCount=$insertedCount, " +
                                            "hasMoreTop=${refreshResult?.hasMoreTop}, " +
                                            "firstIndex=${state.realListState.firstVisibleItemIndex}, " +
                                            "firstOffset=${state.realListState.firstVisibleItemScrollOffset}"
                                )
                            } catch (t: Throwable) {
                                println("[$TAG] Grid pull refresh failed $t")
                            } finally {
                                if (state.isRefreshing) {
                                    state.isRefreshing = false
                                }
                            }
                        }
                    } else if (!state.isRefreshing) {
                        scrollerView.setContentInsetWhenEndDrag(top = 0f)
                    }
                    state.isDraggingFromTop = false
                }
            }
            scrollerView.addScrollerViewEventObserver(observer)
            onDispose {
                scrollerView.removeScrollerViewEventObserver(observer)
                scrollerView.setContentInset(top = 0f, animated = false)
                scrollerView.setContentInsetWhenEndDrag(top = 0f)
                state.isRefreshing = false
                state.pullProgress = 0f
                state.nativeContentOffsetY = 0f
                state.dragStartOffsetY = 0f
                state.isDraggingFromTop = false
                state.skipNextRefreshEndAnimation = false
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { state.pullProgress >= 1f && !state.isRefreshing }
            .collect { shouldRetainOnRelease ->
                val scrollerView = state.scrollerViewRef ?: return@collect
                if (shouldRetainOnRelease) {
                    scrollerView.setContentInsetWhenEndDrag(top = state.refreshThresholdDp)
                } else if (!state.isRefreshing) {
                    scrollerView.setContentInsetWhenEndDrag(top = 0f)
                }
            }
    }

    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) {
            val scrollerView = state.scrollerViewRef ?: return@LaunchedEffect
            if (state.skipNextRefreshEndAnimation) {
                state.skipNextRefreshEndAnimation = false
                return@LaunchedEffect
            }
            scrollerView.setContentInset(top = 0f, animated = true)
            scrollerView.setContentInsetWhenEndDrag(top = 0f)
            delay(GRID_PULL_REFRESH_RESET_DELAY_MS)
            state.pullProgress = 0f
        }
    }
}

private fun finishGridPullRefreshAtTop(
    state: GridPullRefreshState,
    scrollerView: ScrollerView<ScrollerAttr, ScrollerEvent>,
) {
    state.skipNextRefreshEndAnimation = true
    state.isRefreshing = false
    state.pullProgress = 0f
    state.isDraggingFromTop = false
    resetGridPullRefreshInset(state, scrollerView)
}

private fun resetGridPullRefreshInset(
    state: GridPullRefreshState,
    scrollerView: ScrollerView<ScrollerAttr, ScrollerEvent>,
) {
    state.nativeContentOffsetY = 0f
    scrollerView.setContentInsetWhenEndDrag(top = 0f)
    scrollerView.setContentInset(top = 0f, animated = false)
}

/**
 * Grid 下拉刷新指示器浮层（结构对齐 [StaggeredPullRefreshIndicator]）。
 */
@Composable
private fun GridPullRefreshIndicator(
    state: GridPullRefreshState,
    foregroundColor: Color?,
) {
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
        contentAlignment = Alignment.Center,
    ) {
        PullRefreshHeaderView(
            pullProgress = state.pullProgress,
            isRefreshing = state.isRefreshing,
            refreshThreshold = state.refreshThreshold,
            showText = false,
            foregroundColor = foregroundColor,
            lottieTintColor = null,
        )
    }
}

/**
 * LazyVerticalStaggeredGrid - 瀑布流网格
 */
@Composable
private fun StructStaggeredGridView(
    channelWidget: ChannelWidget,
    listState: LazyStaggeredGridStateAdapter,
    displayItems: List<IListItem>,
    dislikeHandler: IDislikeHandler,
    bottomSpace: Float,
    gridConfig: StaggeredGridListConfig,
) {
    val enableHeader = channelWidget.enableHeader()
    val enableFooter = channelWidget.enableFooter()
    val spanSize by remember(gridConfig) { mutableStateOf(gridConfig.gridSpanSize) }
    val realListState = listState.realListState

    // 下拉刷新状态（仅在 enableHeader 时有效）
    val pullRefreshHeaderForegroundColor =
        channelWidget.status.getPullRefreshHeaderForegroundColor()
    val refreshState = rememberStaggeredPullRefreshState(
        enableHeader,
        realListState,
    )

    // 通过 nativeRef 获取底层 ScrollerView，用于下拉刷新控制
    val gridModifier = if (enableHeader) {
        getListModifier(enableHeader).nativeRef { _ ->
            @Suppress("UNCHECKED_CAST")
            val scrollerView = this as? ScrollerView<ScrollerAttr, ScrollerEvent>
            if (scrollerView != null) {
                refreshState.scrollerViewRef = scrollerView
            }
        }
    } else {
        getListModifier(enableHeader)
    }

    Box {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(spanSize),
            state = realListState,
            modifier = gridModifier,
            beyondBoundsItemCount = 8,
            contentPadding = PaddingValues( // 整个列表的边距
                horizontal = gridConfig.gridHorizontalSpace.dp,
                vertical = gridConfig.gridVerticalSpace.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(
                gridConfig.itemHorizontalSpace.dp   // item之间的横向间距
            ),
            verticalItemSpacing = gridConfig.itemVerticalSpace.dp, // item之间的纵向间距
        ) {
            itemsIndexed(
                items = displayItems,
                key = { _, item -> item.flexDto.idStr },
                span = { _, item ->
                    val itemSpanSize = gridConfig.gridSpanConfig?.get(item.flexDto.picShowType)
                        ?: gridConfig.defaultSpanSize
                    if (itemSpanSize >= spanSize || item.flexDto.fullWidthInGrid) {
                        // 全宽item，占据所有列
                        StaggeredGridItemSpan.FullLine
                    } else {
                        // 普通item，占据单列
                        StaggeredGridItemSpan.SingleLane
                    }
                },
                contentType = { _, item -> item.flexDto.picShowType }
            ) { index, feedsItem ->
                val ctx = rememberFeedsItemCtx(
                    index, channelWidget.status.pagerIndex, displayItems.size, dislikeHandler
                )
                RenderFeedsItem(feedsItem, ctx)
            }

            if (enableFooter) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    StructLoadingFooterView(listState, GRID_LOAD_MORE_THRESHOLD)
                }
            }

            // 支持底部安全区，更符合iPhone X 沉浸式滚动的设计
            if (bottomSpace > 0) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SpacerHeight(bottomSpace.fdp)
                }
            }
        }

        // 刷新指示器浮层
        if (enableHeader) {
            PullRefresh4Staggered(
                refreshState = refreshState,
                foregroundColor = pullRefreshHeaderForegroundColor,
            )
        }
    }
}


/**
 * 加载更多footer
 */
@Composable
private fun StructLoadingFooterView(listState: IQnListState, loadMoreThreshold: Int) {
    val pageVM = LocalStructPageViewModel.current ?: return

    val footerStatus by pageVM.controller.footerState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val loadMoreAction = {
        coroutineScope.launch {
            pageVM.refresh(
                FeedsRefreshRequest(
                    refreshForward = ListRefreshForward.BOTTOM_REFRESH,
                    refreshAction = ListRefreshAction.AUTO_MORE
                )
            )
        }
    }

    // 监听滚动到底部事件
    LaunchedEffect(listState, footerStatus) {
        snapshotFlow {
            listState.isScrolledToBottom(loadMoreThreshold)
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && footerStatus == StructListFooterState.WAITING_FOR_MORE) {
                loadMoreAction() // 底部自动加载
            }
        }
    }

    val footerHeightDp = pageVM.controller.rootWidget.action.loadFinishFooterHeightDp
        .takeIf { it > 0 }
        ?: DEFAULT_LOAD_FINISH_FOOTER_HEIGHT_DP

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(footerHeightDp.dp)
            .clickable {
                if (footerStatus == StructListFooterState.ERROR) {
                    loadMoreAction() // 点击重试
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            footerStatus.BuildFooterText()
        }
    }
}

@Composable
private fun StructListFooterState.BuildFooterText() {
    val pageVM = LocalStructPageViewModel.current
    val loadFinishText by remember {
        mutableStateOf(pageVM?.controller?.rootWidget?.getLoadedFinishText())
    }
    val disableLoadFinishText by remember {
        mutableStateOf(pageVM?.controller?.rootWidget?.disableLoadFinishText() ?: false)
    }
    var text by remember { mutableStateOf("") }

    when (this) {
        StructListFooterState.WAITING_FOR_MORE,
        StructListFooterState.LOADING -> {
            ForceNoScale {
                QnLottie(
                    modifier = Modifier.size(16f.dp),
                    fileName = FrameworkLottie.footerLoading,
                    autoPlay = true,
                    infinity = true
                )
            }
            Spacer(modifier = Modifier.width(8f.dp))
            text = "正在加载"
        }

        StructListFooterState.ERROR ->
            text = "加载失败，点击重试"

        StructListFooterState.NO_MORE ->
            text = if (disableLoadFinishText) {
                ""
            } else {
                loadFinishText.takeIfNotBlank() ?: "已加载全部"
            }
    }

    if (text.isNotEmpty()) {
        val rootWidgetAction = pageVM?.controller?.rootWidget?.action
        val fontSizeSp = rootWidgetAction?.loadFinishFooterTextFontSizeSp
            ?.takeIf { it > 0 }
            ?: DEFAULT_LOAD_FINISH_FOOTER_TEXT_FONT_SIZE_SP
        val textColor = when (rootWidgetAction?.loadFinishFooterTextColorLevel) {
            LOAD_FINISH_FOOTER_TEXT_COLOR_LEVEL_T4 -> QNTheme.colorScheme.t4
            else -> QNTheme.colorScheme.t3
        }
        QnText(
            text = text,
            color = textColor,
            fontSize = fontSizeSp.sp
        )
    }
}

@Composable
private fun WatchTopRefresh() {
    // 检查是否有顶部加载更多配置
    val pageVM = LocalStructPageViewModel.current ?: return
    val rootWidget = pageVM.controller.rootWidget
    if (!rootWidget.hasTopMore()) {
        return
    }
    debugWatchTop { "注册监听, rootWidget=${rootWidget::class.simpleName}" }

    val headerStatus by pageVM.controller.headerState.collectAsState()

    val rootListState = LocalStructRootListState.current ?: return
    val listState = LocalStructSelectedListState.current.value ?: return

    val doTopRefresh = {
        // 在下拉时判断是否支持topMore，如果不支持则不创建FeedsRefreshRequest
        if (rootWidget.hasTopMore()) {
            debugWatchTop { "触发 doTopRefresh, rootWidget=${rootWidget::class.simpleName}" }
            pageVM.refresh(FeedsRefreshRequest.topMore())
        }
    }

    // 列表内容区可能由于首刷展示数据不足，list已经触顶了，无法触发滑动；此时也要监听一下rootList来补救
    LaunchedEffect(rootListState, listState) {
        var lastContentOffset = rootListState.contentOffset

        snapshotFlow {
            val isRootScrolling = listState.isScrolledToTop(0) && rootListState.isScrollInProgress
            isRootScrolling to rootListState.contentOffset
        }.collect { (isScrolling, currentOffset) ->
            if (isScrolling) {
                val isScrollingUp = currentOffset < lastContentOffset
                debugWatchTop {
                    "rootScroll: isScrollingUp=$isScrollingUp, " +
                            "currentOffset=$currentOffset, lastOffset=$lastContentOffset, " +
                            "headerStatus=$headerStatus"
                }
                if (headerStatus == StructListHeaderState.WAITING_FOR_MORE && isScrollingUp) {
                    doTopRefresh()
                }
            }
            lastContentOffset = currentOffset // 更新上次的offset
        }
    }

    LaunchedEffect(listState) {
        var isInitialSnapshot = true
        snapshotFlow { // 为了避免进页面就触发，需要限定isScrollInProgress
            val firstIdx = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1
            val isTop = listState.isScrolledToTop(TOP_LOAD_MORE_THRESHOLD)
            val inProgress = listState.isScrollInProgress
            Triple(isTop && !inProgress, firstIdx, inProgress)
        }.collect { (shouldLoadTopMore, firstIdx, inProgress) ->
            debugWatchTop {
                "skip initial listScroll: shouldLoadTopMore=$shouldLoadTopMore, " +
                        "firstVisibleIdx=$firstIdx, inProgress=$inProgress, headerStatus=$headerStatus"
            }
            if (shouldLoadTopMore) {
                debugWatchTop {
                    "listScroll: firstVisibleIdx=$firstIdx, " +
                            "inProgress=$inProgress, headerStatus=$headerStatus"
                }
            }
            if (shouldLoadTopMore && headerStatus == StructListHeaderState.WAITING_FOR_MORE) {
                doTopRefresh()
            }
        }
    }
}

private inline fun debugWatchTop(msg: () -> String) {
    ComposeViewLog.debug("WatchTop", msg)
}
