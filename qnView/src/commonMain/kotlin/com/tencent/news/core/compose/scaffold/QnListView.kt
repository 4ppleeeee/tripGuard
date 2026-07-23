package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.animation.core.FastOutSlowInEasing
import com.tencent.kuikly.compose.animation.core.animateDpAsState
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.gestures.awaitEachGesture
import com.tencent.kuikly.compose.foundation.gestures.awaitFirstDown
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
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
import com.tencent.kuikly.compose.foundation.pager.PageSize
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.foundation.pager.VerticalPager
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose_dsl.kuikly.Views.pullToRefreshItem
import com.tencent.kuikly.compose_dsl.kuikly.Views.rememberPullToRefreshState
import com.tencent.kuikly.compose_dsl.kuikly.extension.NestedScrollMode
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose_dsl.kuikly.extension.flingSpeedLimit
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.kuikly.compose_dsl.kuikly.extension.nestedScroll
import com.tencent.kuikly.core.views.IScrollerViewEventObserver
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.ScrollerAttr
import com.tencent.kuikly.core.views.ScrollerEvent
import com.tencent.kuikly.core.views.ScrollerView
import com.tencent.news.core.channel.constants.NewsChannel
import com.tencent.news.core.compose.platform.safeAreaHeight
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.card.FeedsItemCtx
import com.tencent.news.core.compose.scaffold.card.IDislikeHandler
import com.tencent.news.core.compose.scaffold.modifiers.willAppear
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.registry.LocalStructRootListState
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.scaffold.theme.QnSkin
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
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
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.model.NormalListConfig
import com.tencent.news.core.list.model.QnListConfig
import com.tencent.news.core.list.model.StaggeredGridListConfig
import com.tencent.news.core.list.model.VerticalPagerListConfig
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.ChannelListBackgroundStyle
import com.tencent.news.core.page.model.ChannelListDecorationState
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.ChannelWidget.Companion.enableFooter
import com.tencent.news.core.page.model.ChannelWidget.Companion.enableHeader
import com.tencent.news.core.page.model.ChannelWidgetStatus
import com.tencent.news.core.page.model.StructListFooterState
import com.tencent.news.core.page.model.StructListHeaderState
import com.tencent.news.core.platform.Lotties
import com.tencent.news.core.service.ViewService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.tencent.kuikly.compose.ui.graphics.Color as ComposeColor

val LocalVerticalPagerBoundaryScrollBlocker = staticCompositionLocalOf<(Boolean) -> Unit> { {} }

private const val LIST_LOAD_MORE_THRESHOLD = 2    // 列表布局提前2个item触发加载更多
private const val GRID_LOAD_MORE_THRESHOLD = 4    // 网格和瀑布流布局提前4个item触发加载更多
private const val TOP_LOAD_MORE_THRESHOLD = 2     // 顶部加载更多阈值：滚动到前2个item时触发（前2条件）
private const val DEFAULT_LIST_BEYOND_BOUNDS_ITEM_COUNT = 6
private const val MEMBER_LIST_BEYOND_BOUNDS_ITEM_COUNT = 12

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
            !dislikedItemIds.contains(item.baseDto.idStr)
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

    // 监听顶部刷新（用于在列表触顶且整体也到顶时，通过手势检测触发顶部加载）
    val pullingDownState = remember { mutableStateOf(false) }

    val listConfig = channelWidget.status.getListConfig()
    val flingSpeedLimit = channelWidget.findStructPageWidget2()?.pageConfig?.flingSpeedLimit
    val enableFooter = channelWidget.enableFooter()
    val enableHeader = channelWidget.enableHeader()
    val pageVM = LocalStructPageViewModel.current
    val bottomSpace = buildListBottomSpace()
    val beyondBoundsItemCount = channelWidget.listBeyondBoundsItemCount()
    val isDarkTheme = isAppInDarkTheme()
    val pullRefreshHeaderVM = remember(pageVM) {
        pageVM?.pullRefreshHeaderViewModel
    }
    LaunchedEffect(pullRefreshHeaderVM, isDarkTheme) {
        pullRefreshHeaderVM?.onThemeChanged(isDarkTheme)
    }
    val refreshHeaderSkin by pullRefreshHeaderVM?.uiState?.collectAsState()
        ?: remember(isDarkTheme) { mutableStateOf(PullRefreshHeaderUiState.default(isDarkTheme)) }
    val pullRefreshResultState = remember(pageVM) { PullRefreshResultState() }

    // 所有列表类型统一创建 listState
    // 当开启滚动位置恢复时，优先使用保存的滚动位置
    val enableRestore = channelWidget.status.enableScrollPositionRestore
    val initIndex = if (enableRestore) {
        channelWidget.status.userScrolledIndex ?: channelWidget.status.initIndex.takeIf { it > 0 }
        ?: 0
    } else {
        channelWidget.status.initIndex.takeIf { it > 0 } ?: 0
    }
    val initOffset = if (enableRestore) {
        channelWidget.status.userScrolledOffset ?: 0
    } else {
        0
    }
    val isSelected by channelWidget.status.isSelected.collectAsState()
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
    WatchTopRefresh(
        listState = listState,
        isPullingDown = { pullingDownState.value },
        isTabSelected = { isSelected }
    )
    if (isSelected) {
        if (selectedListState.value != listState) {
            selectedListState.value = listState
        }
    }

    // 实时更新可见 item 的 index 范围，供 VM 层判断 targetItem 是否已在屏幕内
    LaunchedEffect(listState) {
        snapshotFlow {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            Pair(visibleItems.firstOrNull()?.index, visibleItems.lastOrNull()?.index)
        }.collect { (firstIndex, lastIndex) ->
            channelWidget.status.firstVisibleItemIndex = firstIndex
            channelWidget.status.lastVisibleItemIndex = lastIndex
        }
    }

    when (listState) {
        is LazyListStateAdapter -> StructListView(
            listState = listState,
            enableFooter = enableFooter,
            enableHeader = enableHeader,
            displayItems = finalDisplayItems,
            dislikeHandler = dislikeHandler,
            bottomSpace = bottomSpace,
            listConfig = listConfig as NormalListConfig,
            beyondBoundsItemCount = beyondBoundsItemCount,
            onPullingDownChange = { pullingDownState.value = it },
            flingSpeedLimit = flingSpeedLimit,
            listDecorationState = channelWidget.status.listDecorationState,
            refreshHeaderSkin = refreshHeaderSkin,
            pullRefreshResultState = pullRefreshResultState,
        )

        is LazyGridStateAdapter -> StructGridView(
            listState = listState,
            enableFooter = enableFooter,
            displayItems = finalDisplayItems,
            dislikeHandler = dislikeHandler,
            gridConfig = listConfig as GridListConfig,
            flingSpeedLimit = flingSpeedLimit,
        )

        is LazyStaggeredGridStateAdapter -> StructStaggeredGridView(
            listState = listState,
            enableFooter = enableFooter,
            enableHeader = enableHeader,
            displayItems = finalDisplayItems,
            dislikeHandler = dislikeHandler,
            gridConfig = listConfig as StaggeredGridListConfig,
            flingSpeedLimit = flingSpeedLimit,
            refreshHeaderSkin = refreshHeaderSkin,
            pullRefreshResultState = pullRefreshResultState,
        )

        is PagerStateAdapter -> StructVerticalPagerView(
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

private fun ChannelWidget.listBeyondBoundsItemCount(): Int {
    return if (data?.channel_info?.channelKey == NewsChannel.MEMBER) {
        MEMBER_LIST_BEYOND_BOUNDS_ITEM_COUNT
    } else {
        DEFAULT_LIST_BEYOND_BOUNDS_ITEM_COUNT
    }
}

@Composable
private fun QnListConfig.createListState(
    initIndex: Int,
    initOffset: Int,
    itemCount: Int
): IQnListState {
    return when (this) {
        is NormalListConfig -> {
            val listState = rememberLazyListState(initIndex, initOffset)
            remember { LazyListStateAdapter(listState) }
        }

        is GridListConfig -> {
            val listState = rememberLazyGridState(initIndex, initOffset)
            remember { LazyGridStateAdapter(listState) }
        }

        is StaggeredGridListConfig -> {
            val listState = rememberLazyStaggeredGridState(initIndex, initOffset)
            remember { LazyStaggeredGridStateAdapter(listState) }
        }

        is VerticalPagerListConfig -> {
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
    val windowSizeType = WindowSizeUtils.getWindowSizeType()
    val config by remember(windowSizeType) {
        mutableStateOf(
            when (windowSizeType) {
                WindowSizeType.SUPER_BIG -> superBigWindowListConfig
                WindowSizeType.BIG -> bigWindowListConfig
                WindowSizeType.NORMAL -> normalListConfig
            }
        )
    }
    return config
}

@Composable
private fun buildListBottomSpace(): Float {
    val config = LocalStructPageViewModel.current?.pageRootWidget?.pageConfig
        ?: return 0f
    val bottomSafeArea = if (config.expandBottomSafeAreaForList) {
        safeAreaHeight()
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
            onDislike(item.baseDto.idStr)
        }
    }
}

/**
 * 创建FeedsItemCtx
 */
@Composable
private fun rememberFeedsItemCtx(
    index: Int,
    displayItemsSize: Int,
    dislikeHandler: IDislikeHandler,
): FeedsItemCtx {
    return remember(index, displayItemsSize) {
        FeedsItemCtx(
            indexInList = index,
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
    listCtx: FeedsItemCtx,
) {
    ViewService.itemCard.Build(feedsItem, listCtx)
}


@Composable
private fun getListModifier(
    enableHeader: Boolean = false,
    bouncesEnable: Boolean = false,
    flingSpeedLimit: Float? = null,
): Modifier {
    val page = LocalActivity.current as? ComposePage
    // 任一条件为 true 即启用 bounces：
    //  - enableHeader=true：为了支持下拉刷新
    //  - bouncesEnable=true：业务独立启用回弹效果（不依赖下拉刷新）
    val enableBounces = enableHeader || bouncesEnable
    var modifier = Modifier
        .fillMaxSize()
        .willAppear { page?.onPageFirstFrameRendered() }
        .nestedScroll(
            if (enableHeader) NestedScrollMode.SELF_FIRST else NestedScrollMode.PARENT_FIRST,
            NestedScrollMode.SELF_FIRST
        )
        .bouncesEnable(enableBounces)  // 启用bounces以支持下拉刷新或独立回弹效果
    flingSpeedLimit?.let {
        modifier = modifier.flingSpeedLimit(it)
    }
    return modifier
}

/**
 * VerticalPager - 垂直分页滚动
 */
@Composable
private fun StructVerticalPagerView(
    listState: PagerStateAdapter,
    displayItems: List<IKmmFeedsItem>,
    dislikeHandler: IDislikeHandler,
    verticalPagerConfig: VerticalPagerListConfig,
) {
    val page = LocalActivity.current as? ComposePage
    val pageVM = LocalStructPageViewModel.current
    // 从listState中获取pagerState
    val pagerState = listState.realListState as PagerState

    // 使用 rememberUpdatedState 确保在 LaunchedEffect 中始终获取最新的 displayItems
    val currentDisplayItems by rememberUpdatedState(displayItems)

    // 从verticalPagerConfig中获取分页配置
    val userScrollEnabled = verticalPagerConfig.userScrollEnabled

    if (displayItems.isEmpty()) {
        // 空状态处理
        Box(modifier = Modifier.fillMaxSize())
    } else {

        // 监听页面切换，更新全局索引（用于广场item插入位置计算）
        LaunchedEffect(pagerState, displayItems.size) {
            snapshotFlow { pagerState.currentPage }.collect { currentPage ->
                verticalPagerConfig.onPageChanged?.invoke(currentPage)
                (pageVM as? IStructVerticalPagerLifecycle)?.updateCurrentVisibleIndex(currentPage)
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

        // 用于检测底部边界滑动（累计向上滑动距离）
        var accumulatedUpDrag by remember { mutableStateOf(0f) }
        // 用于检测顶部边界滑动（累计向下滑动距离）
        var accumulatedDownDrag by remember { mutableStateOf(0f) }
        // 边界滑动检测阈值（像素），用户滑动超过此距离触发提示
        val boundaryScrollThreshold = 50f
        // 是否位于最后一页
        var atBottomBoundary by remember { mutableStateOf(false) }
        // 是否位于第一页
        var atTopBoundary by remember { mutableStateOf(false) }
        val boundaryScrollBlockedState = remember { mutableStateOf(false) }

        // 监听当前页面变化，判断当前是否位于顶部/底部边界
        LaunchedEffect(pagerState, displayItems.size) {
            snapshotFlow { pagerState.currentPage }.collect { currentPage ->
                atBottomBoundary = currentPage == displayItems.size - 1
                atTopBoundary = currentPage == 0
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(
                LocalVerticalPagerBoundaryScrollBlocker provides { blocked ->
                    boundaryScrollBlockedState.value = blocked
                }
            ) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .willAppear { page?.onPageFirstFrameRendered() }
                        .nestedScroll(NestedScrollMode.SELF_ONLY, NestedScrollMode.SELF_ONLY)
                        .bouncesEnable(false)
                        .pointerInput(atBottomBoundary, atTopBoundary, displayItems.size) {
                            // 当在边界页时，监听越界滑动手势（不消费手势，只做检测）
                            if (atBottomBoundary || atTopBoundary) {
                                awaitEachGesture {
                                    // 等待按下事件（不消费，允许穿透）
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    accumulatedUpDrag = 0f
                                    accumulatedDownDrag = 0f
                                    var boundaryScrollBlockedDuringGesture = boundaryScrollBlockedState.value
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
                                        boundaryScrollBlockedDuringGesture =
                                            boundaryScrollBlockedDuringGesture || boundaryScrollBlockedState.value

                                        // dragAmount < 0 表示向上滑动（手指向上移动）
                                        if (dragAmount < 0) {
                                            accumulatedUpDrag += dragAmount
                                        }
                                        // dragAmount > 0 表示向下滑动（手指向下移动）
                                        if (dragAmount > 0) {
                                            accumulatedDownDrag += dragAmount
                                        }
                                        // 不消费手势，让VerticalPager可以正常处理滑动
                                    }

                                    // 拖动结束，检查是否触发边界滑动
                                    if (!boundaryScrollBlockedDuringGesture &&
                                        atBottomBoundary &&
                                        accumulatedUpDrag < -boundaryScrollThreshold
                                    ) {
                                        // 用户在最后一页向上滑动超过阈值
                                        (pageVM as? IStructVerticalPagerLifecycle)?.onBoundaryScrollAttempt()
                                    }
                                    if (!boundaryScrollBlockedDuringGesture &&
                                        atTopBoundary &&
                                        accumulatedDownDrag > boundaryScrollThreshold
                                    ) {
                                        // 用户在第一页向下滑动超过阈值
                                        (pageVM as? IStructVerticalPagerLifecycle)?.onTopBoundaryScrollAttempt()
                                    }
                                    accumulatedUpDrag = 0f
                                    accumulatedDownDrag = 0f
                                }
                            }
                        },
                    pageSize = PageSize.Fill,
                    beyondViewportPageCount = 1,
                    pageSpacing = 0.dp,
                    userScrollEnabled = userScrollEnabled,
                    key = { displayItems[it].baseDto.idStr },
                ) { index ->
                    val feedsItem = displayItems[index]

                    val ctx = rememberFeedsItemCtx(
                        index,
                        displayItems.size,
                        dislikeHandler
                    )
                    RenderFeedsItem(feedsItem, ctx)
                }
            }
        }
    }
}

/**
 * LazyColumn
 */
@Composable
private fun StructListView(
    listState: LazyListStateAdapter,
    enableFooter: Boolean,
    enableHeader: Boolean,
    displayItems: List<IListItem>,
    dislikeHandler: IDislikeHandler,
    bottomSpace: Float,
    listConfig: NormalListConfig,
    beyondBoundsItemCount: Int,
    onPullingDownChange: ((Boolean) -> Unit)? = null,
    flingSpeedLimit: Float?,
    listDecorationState: ChannelListDecorationState,
    refreshHeaderSkin: PullRefreshHeaderUiState,
    pullRefreshResultState: PullRefreshResultState,
) {
    val realListState = listState.realListState
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState(isRefreshing)

    val topPadding = listConfig.topPadding
    val coroutineScope = rememberCoroutineScope()
    val pageVM = LocalStructPageViewModel.current

    // 当列表触顶时，通过 pointerInput 检测用户下划手势趋势，通知 WatchTopRefresh
    val pullingDownModifier = if (onPullingDownChange != null) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var lastY = down.position.y
                var isPulling = false
                while (true) {
                    val event = awaitPointerEvent()
                    val drag = event.changes.find { it.id == down.id } ?: break
                    if (!drag.pressed) break
                    val currentY = drag.position.y
                    val dragAmount = currentY - lastY
                    lastY = currentY
                    // dragAmount > 0 表示手指向下移动（下划手势）
                    val atTop = realListState.firstVisibleItemIndex == 0 &&
                            realListState.firstVisibleItemScrollOffset == 0
                    val newIsPulling = atTop && dragAmount > 0
                    if (newIsPulling != isPulling) {
                        isPulling = newIsPulling
                        onPullingDownChange(isPulling)
                    }
                }
                // 手势结束，重置状态
                if (isPulling) {
                    onPullingDownChange(false)
                }
            }
        }
    } else Modifier
    val listBackgroundModifier = listDecorationState.listBackgroundStyle.toModifier()
    val sectionBackgroundStartAfterItemId = listDecorationState.sectionBackgroundStartAfterItemId
    val sectionBackgroundStartIndex = remember(sectionBackgroundStartAfterItemId, displayItems) {
        displayItems.indexOfFirst { item ->
            item.baseDto.idStr == sectionBackgroundStartAfterItemId
        }
    }
    val hasSectionBackground = listDecorationState.sectionBackgroundStyle != ChannelListBackgroundStyle.NONE &&
            sectionBackgroundStartIndex >= 0
    val showPullRefreshResult = enableHeader &&
            pullRefreshResultState.isVisible &&
            pullRefreshResultState.text.isNotBlank()
    val pullRefreshResultHeight by animateDpAsState(
        targetValue = if (showPullRefreshResult) PULL_REFRESH_RESULT_HEIGHT else 0.dp,
        animationSpec = tween(
            durationMillis = if (showPullRefreshResult) {
                PULL_REFRESH_RESULT_ENTER_ANIMATION_DURATION_MS
            } else {
                PULL_REFRESH_RESULT_EXIT_ANIMATION_DURATION_MS
            },
            easing = FastOutSlowInEasing
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        state = realListState,
        beyondBoundsItemCount = beyondBoundsItemCount, // 预加载个数(个数太少快速滑动白屏)
        modifier = getListModifier(enableHeader, listConfig.bouncesEnable, flingSpeedLimit)
            .then(listBackgroundModifier)
            .then(pullingDownModifier),
        contentPadding = remember(topPadding, pullRefreshResultHeight) {
            PaddingValues(top = topPadding.dp + pullRefreshResultHeight)
        }
    ) {
        if (enableHeader) {

            pullToRefreshItem(
                state = pullToRefreshState,
                onRefresh = {
                    coroutineScope.launch {
                        val vm = pageVM ?: return@launch
                        pullRefreshResultState.clear()
                        isRefreshing = true
                        val result = vm.onPullRefresh()
                        if (result.shouldShowResult) {
                            val resultVersion = pullRefreshResultState.showNow(result.text)
                            isRefreshing = false
                            pullRefreshResultState.hideAfterDelay(resultVersion)
                        } else {
                            isRefreshing = false
                        }
                    }
                },
                scrollState = realListState,
                content = { pullProgress: Float, isRefreshing: Boolean, refreshThreshold: Dp ->
                    PullRefreshHeaderView(
                        pullProgress = pullProgress,
                        isRefreshing = isRefreshing,
                        refreshThreshold = refreshThreshold,
                        refreshHeaderSkin = refreshHeaderSkin,
                        pullRefreshResultState = pullRefreshResultState,
                        showResultInHeader = false
                    )
                }
            )
        }

        itemsIndexed(
            items = displayItems,
            key = { _, item -> item.baseDto.idStr },
            contentType = { _, item -> item.baseDto.picShowType }
        ) { index, feedsItem ->
            val ctx = rememberFeedsItemCtx(index, displayItems.size, dislikeHandler)
            if (hasSectionBackground && index > sectionBackgroundStartIndex) {
                Box(modifier = Modifier.fillMaxWidth().then(listDecorationState.sectionBackgroundStyle.toModifier())) {
                    RenderFeedsItem(feedsItem, ctx)
                }
            } else {
                RenderFeedsItem(feedsItem, ctx)
            }
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
                SpacerHeight(bottomSpace)
            }
        }
    }
        if (pullRefreshResultHeight > 0.dp && pullRefreshResultState.text.isNotBlank()) {
            PullRefreshResultBar(
                refreshHeaderSkin = refreshHeaderSkin,
                pullRefreshResultState = pullRefreshResultState,
                height = pullRefreshResultHeight
            )
        }
    }
}

@Composable
private fun ChannelListBackgroundStyle.toModifier(): Modifier {
    return when (this) {
        ChannelListBackgroundStyle.NONE -> Modifier
        ChannelListBackgroundStyle.BG_BLOCK -> Modifier.background(QNTheme.colorScheme.bgBlock)
        ChannelListBackgroundStyle.BG_PAGE -> Modifier.background(QNTheme.colorScheme.bgPage)
    }
}

/**
 * LazyVerticalGrid - 标准网格布局
 */
@Composable
private fun StructGridView(
    listState: LazyGridStateAdapter,
    enableFooter: Boolean,
    displayItems: List<IListItem>,
    dislikeHandler: IDislikeHandler,
    gridConfig: GridListConfig,
    flingSpeedLimit: Float?,
) {
    val spanSize by remember(gridConfig) { mutableStateOf(gridConfig.gridSpanSize) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(spanSize),
        state = listState.realListState,
        modifier = getListModifier(flingSpeedLimit = flingSpeedLimit)
    ) {
        itemsIndexed(
            items = displayItems,
            key = { _, item -> item.baseDto.idStr },
            span = { _, item ->
                val itemSpanSize =
                    gridConfig.gridSpanConfig?.get(item.baseDto.picShowType) ?: spanSize
                GridItemSpan(itemSpanSize)
            },
            contentType = { _, item -> item.baseDto.picShowType }
        ) { index, feedsItem ->
            val ctx = rememberFeedsItemCtx(index, displayItems.size, dislikeHandler)
            RenderFeedsItem(feedsItem, ctx)
        }

        // 添加Footer
        if (enableFooter) {
            item(span = { GridItemSpan(spanSize) }) {
                StructLoadingFooterView(listState, GRID_LOAD_MORE_THRESHOLD)
            }
        }
    }
}

/**
 * LazyVerticalStaggeredGrid - 瀑布流网格
 */
@Composable
private fun StructStaggeredGridView(
    listState: LazyStaggeredGridStateAdapter,
    enableFooter: Boolean,
    enableHeader: Boolean,
    displayItems: List<IListItem>,
    dislikeHandler: IDislikeHandler,
    gridConfig: StaggeredGridListConfig,
    flingSpeedLimit: Float?,
    refreshHeaderSkin: PullRefreshHeaderUiState,
    pullRefreshResultState: PullRefreshResultState,
) {
    val spanSize by remember(gridConfig) { mutableStateOf(gridConfig.gridSpanSize) }
    val realListState = listState.realListState

    // 下拉刷新状态（仅在 enableHeader 时有效）
    val refreshState = rememberStaggeredPullRefreshState(enableHeader, realListState)

    // 通过 nativeRef 获取底层 ScrollerView，用于下拉刷新控制
    val gridModifier = if (enableHeader) {
        getListModifier(enableHeader, flingSpeedLimit = flingSpeedLimit).nativeRef { _ ->
            @Suppress("UNCHECKED_CAST")
            val scrollerView = this as? ScrollerView<ScrollerAttr, ScrollerEvent>
            if (scrollerView != null) {
                refreshState.scrollerViewRef = scrollerView
            }
        }
    } else {
        getListModifier(enableHeader, flingSpeedLimit = flingSpeedLimit)
    }

    // 注册下拉刷新的 ScrollerView observer 和副作用
    if (enableHeader) {
        StaggeredPullRefreshEffects(refreshState, pullRefreshResultState)
    }

    Box {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(spanSize),
            state = realListState,
            modifier = gridModifier,
            beyondBoundsItemCount = 8,
            horizontalArrangement = Arrangement.spacedBy(gridConfig.horizontalSpacing.dp),
            verticalItemSpacing = gridConfig.verticalSpacing.dp,
        ) {
            itemsIndexed(
                items = displayItems,
                key = { _, item -> item.baseDto.idStr },
                span = { _, item ->
                    val itemSpanSize =
                        gridConfig.gridSpanConfig?.get(item.baseDto.picShowType) ?: spanSize
                    if (itemSpanSize >= spanSize) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                },
                contentType = { _, item -> item.baseDto.picShowType }
            ) { index, feedsItem ->
                val ctx = rememberFeedsItemCtx(index, displayItems.size, dislikeHandler)
                RenderFeedsItem(feedsItem, ctx)
            }

            if (enableFooter) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    StructLoadingFooterView(listState, GRID_LOAD_MORE_THRESHOLD)
                }
            }
        }

        // 刷新指示器浮层
        if (enableHeader) {
            StaggeredPullRefreshIndicator(refreshState, refreshHeaderSkin, pullRefreshResultState)
        }
    }
}


private val STAGGERED_REFRESH_THRESHOLD = 94.dp // 头部组件的 text + distance + lottie 高度

/**
 * 瀑布流下拉刷新状态容器
 */
private class StaggeredPullRefreshState(
    val realListState: com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState,
) {
    var isRefreshing by mutableStateOf(false)
    var pullProgress by mutableStateOf(0f)
    var scrollerViewRef by mutableStateOf<ScrollerView<ScrollerAttr, ScrollerEvent>?>(null)
    // 用于指示器定位的原生 offsetY（dp 值），由 observer 回调更新
    var nativeContentOffsetY by mutableStateOf(0f)
    // 记录 dragBegin 时的 offsetY，用于计算下拉距离（差值）
    var dragStartOffsetY = 0f
    // 标记当前拖拽是否从顶部开始
    var isDraggingFromTop = false

    val refreshThreshold = STAGGERED_REFRESH_THRESHOLD
    val refreshThresholdDp get() = refreshThreshold.value

    /** 判断列表是否在顶部 */
    fun isAtTop(): Boolean {
        val firstIndex = realListState.firstVisibleItemIndex
        val firstOffset = realListState.firstVisibleItemScrollOffset
        return firstIndex == 0 && firstOffset <= 0
    }
}

@Composable
private fun rememberStaggeredPullRefreshState(
    enableHeader: Boolean,
    realListState: com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState,
): StaggeredPullRefreshState {
    return remember(enableHeader, realListState) {
        StaggeredPullRefreshState(realListState)
    }
}

/**
 * 瀑布流下拉刷新副作用管理
 * 包含：ScrollerView observer 注册、pullProgress 监听、刷新完成收起指示器
 */
@Composable
private fun StaggeredPullRefreshEffects(
    state: StaggeredPullRefreshState,
    pullRefreshResultState: PullRefreshResultState
) {
    val coroutineScope = rememberCoroutineScope()
    val pageVM = LocalStructPageViewModel.current

    // 注册 ScrollerView 的 observer：更新 pullProgress 和在松手时判断是否触发刷新
    if (state.scrollerViewRef != null) {
        DisposableEffect(state.scrollerViewRef) {
            val scrollerView = state.scrollerViewRef ?: return@DisposableEffect onDispose { }
            val observer = object : IScrollerViewEventObserver {
                override fun onContentOffsetDidChanged(
                    offsetX: Float,
                    offsetY: Float,
                    params: ScrollParams
                ) {
                    state.nativeContentOffsetY = offsetY

                    if (state.isDraggingFromTop && params.isDragging && state.isAtTop()) {
                        // 下拉距离 = dragStartOffsetY - 当前 offsetY（下拉时 offsetY 减小）
                        val pullDistance = state.dragStartOffsetY - offsetY
                        state.pullProgress = if (pullDistance > 0) {
                            (pullDistance / state.refreshThresholdDp).coerceIn(0f, 1f)
                        } else {
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
                        pullRefreshResultState.clear()
                        state.isRefreshing = true
                        scrollerView.setContentInset(
                            top = state.refreshThresholdDp,
                            animated = true
                        ) // 参数单位：dp

                        coroutineScope.launch {
                            val vm = pageVM ?: run {
                                state.isRefreshing = false
                                return@launch
                            }
                            val result = vm.onPullRefresh()
                            if (result.shouldShowResult) {
                                scrollerView.setContentInset(
                                    top = PULL_REFRESH_RESULT_HEIGHT.value,
                                    animated = true
                                )
                                scrollerView.setContentInsetWhenEndDrag(
                                    top = PULL_REFRESH_RESULT_HEIGHT.value
                                )
                                pullRefreshResultState.show(result.text)
                            }
                            state.isRefreshing = false
                        }
                    } else if (!state.isRefreshing) {
                        scrollerView.setContentInsetWhenEndDrag(top = 0f) // 参数单位：dp
                    }
                    // 松手后重置拖拽状态
                    state.isDraggingFromTop = false
                }
            }
            scrollerView.addScrollerViewEventObserver(observer)
            onDispose {
                scrollerView.removeScrollerViewEventObserver(observer)
            }
        }
    }

    // 监听 pullProgress 变化，动态设置松手回弹位置（预设 inset，确保松手后停在正确位置）
    LaunchedEffect(Unit) {
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

    // 监听刷新完成：收起指示器
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) {
            val scrollerView = state.scrollerViewRef ?: return@LaunchedEffect
            scrollerView.setContentInset(top = 0f, animated = true) // 参数单位：dp
            scrollerView.setContentInsetWhenEndDrag(top = 0f) // 参数单位：dp
            // 短暂延迟后再重置状态，避免快速连续下拉刷新时指示器闪烁
            delay(300)
            state.pullProgress = 0f
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
    refreshHeaderSkin: PullRefreshHeaderUiState,
    pullRefreshResultState: PullRefreshResultState
) {
    // 鸿蒙的 ScrollerView 表现与安卓不一致：
    // 当下拉刷新松手时，ScrollerView 会自动将 contentInset 设置为 0，导致浮层被隐藏
    val indicatorHeight = if (pullRefreshResultState.isVisible) {
        PULL_REFRESH_RESULT_HEIGHT
    } else {
        state.refreshThreshold
    }
    val indicatorOffsetY = when {
        pullRefreshResultState.isVisible -> 0.dp

        isHarmonyPlatform() && state.isRefreshing -> {
            if (state.nativeContentOffsetY < 0f) {
                (-indicatorHeight + state.nativeContentOffsetY.unaryMinus().dp)
                    .coerceAtLeast(0.dp)
            } else {
                -(state.nativeContentOffsetY.coerceAtMost(state.refreshThresholdDp)).dp
            }
        }

        else -> {
            -indicatorHeight + state.nativeContentOffsetY.coerceAtMost(0f).unaryMinus().dp
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(indicatorHeight)
            .offset(y = indicatorOffsetY),
        contentAlignment = Alignment.Center
    ) {
        PullRefreshHeaderView(
            pullProgress = state.pullProgress,
            isRefreshing = state.isRefreshing,
            refreshThreshold = state.refreshThreshold,
            refreshHeaderSkin = refreshHeaderSkin,
            pullRefreshResultState = pullRefreshResultState
        )
    }
}


/**
 * 加载更多footer
 */
@Composable
private fun StructLoadingFooterView(
    listState: IQnListState,
    loadMoreThreshold: Int,
) {
    val pageVM = LocalStructPageViewModel.current ?: return

    val footerStatus by pageVM.controller.footerState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val loadMoreAction = loadMoreAction@{
        val latestFooterStatus = pageVM.controller.footerState.value
        if (latestFooterStatus == StructListFooterState.LOADING || latestFooterStatus == StructListFooterState.NO_MORE) {
            return@loadMoreAction
        }
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48f.dp)
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
    var text by remember { mutableStateOf("") }

    when (this) {
        StructListFooterState.WAITING_FOR_MORE,
        StructListFooterState.LOADING -> {
            QnLottie(
                modifier = Modifier.size(16f.dp),
                fileName = Lotties.footerLoading,
                autoPlay = true,
                infinity = true
            )
            Spacer(modifier = Modifier.width(8f.dp))
            text = "正在加载"
        }

        StructListFooterState.ERROR ->
            text = "加载失败，点击重试"

        StructListFooterState.NO_MORE ->
            text = loadFinishText.takeIfNotBlank() ?: "已加载全部"
    }
    QnText(
        text = text,
        // 对齐 TencentNews QaEventSubTabFragment.applyPageSkinInner：有皮肤背景时 footer 用白字（t4），
        // 避免灰字（t3）糊在皮肤底色里看不清；无皮肤时维持默认 t3。
        color = if (QnSkin != null) QNTheme.colorScheme.t4 else QNTheme.colorScheme.t3,
        fontSize = 14.sp
    )
}

@Composable
private fun WatchTopRefresh(
    listState: IQnListState,
    isPullingDown: () -> Boolean = { false },
    isTabSelected: () -> Boolean = { true }
) {
    // 检查是否有顶部加载更多配置
    val pageVM = LocalStructPageViewModel.current ?: return
    val rootWidget = pageVM.controller.rootWidget
    if (!rootWidget.hasTopMore()) {
        return
    }

    val headerStatus by pageVM.controller.headerState.collectAsState()

    val rootListState = LocalStructRootListState.current ?: return

    val isLocatingArticle by pageVM.controller.isLocatingArticle.collectAsState()

    val doTopRefresh = {
        // 在下拉时判断是否支持topMore，如果不支持则不创建FeedsRefreshRequest
        val canTopMore = rootWidget.hasTopMore()
        // 正在定位文章时，忽略顶部自动加载
        if (canTopMore && headerStatus != StructListHeaderState.LOADING && !isLocatingArticle) {
            // 执行刷新操作
            pageVM.refresh(
                FeedsRefreshRequest(
                    refreshForward = ListRefreshForward.TOP_REFRESH,
                    refreshAction = ListRefreshAction.AUTO_MORE
                )
            )
        }
    }

    // 列表内容区可能由于首刷展示数据不足，list已经触顶了，无法触发滑动；此时也要监听一下rootList来补救
    LaunchedEffect(rootListState, listState) {
        var lastContentOffset = rootListState.contentOffset

        snapshotFlow {
            val isListAtTop = listState.isScrolledToTop(0)
            val isRootScrolling = isListAtTop && rootListState.isScrollInProgress
            // 将 isPullingDown() 也纳入追踪，确保手势状态变化时 snapshotFlow 能重新触发
            val pullingDown = isPullingDown()
            Triple(isRootScrolling to pullingDown, isListAtTop, rootListState.contentOffset)
        }.collect { (scrollingAndPulling, isListAtTop, currentOffset) ->
            val (isScrolling, pullingDown) = scrollingAndPulling
            if (headerStatus == StructListHeaderState.WAITING_FOR_MORE) {
                if (isScrolling) {
                    // 场景1：rootList 正在滚动，子列表已触顶 —— 检测向上滑动（offset 减小）
                    val isScrollingUp = currentOffset < lastContentOffset
                    if (isScrollingUp) {
                        doTopRefresh()
                    }
                } else if (isListAtTop && !rootListState.isScrollInProgress) {
                    // 场景2：子列表触顶且 rootList 也无法滚动（整体已到顶，无 bounces 弹性效果），
                    // 通过 pointerInput 手势检测用户是否有下划趋势
                    // 只有当前 tab 被选中时才检查手势趋势，避免后台 tab 干扰
                    if (pullingDown && isTabSelected()) {
                        doTopRefresh()
                    }
                }
            }
            lastContentOffset = currentOffset // 更新上次的offset
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { // 为了避免进页面就触发，需要限定isScrollInProgress
            listState.isScrolledToTop(TOP_LOAD_MORE_THRESHOLD) && listState.isScrollInProgress
        }.collect { shouldLoadTopMore ->
            val tabSelected = isTabSelected()
            if (shouldLoadTopMore && headerStatus == StructListHeaderState.WAITING_FOR_MORE) {
                // 只有当前 tab 被选中时才允许触发顶部加载，避免后台 tab 干扰
                if (tabSelected) {
                    doTopRefresh()
                }
            }
        }
    }
}

// 下拉刷新 lottie 高度（对齐 PullHeadView 中 earth 的高度 50dp）
private val PULL_LOTTIE_HEIGHT = 50.dp

// 文字高度估算 + lottie 与文字间距（对齐 PullHeadView 中 DISTANCE = 30dp）
private val PULL_LOTTIE_SCALE_START_OFFSET = 30.dp

private val PULL_REFRESH_RESULT_HEIGHT = 40.dp
private const val PULL_REFRESH_RESULT_ENTER_ANIMATION_DURATION_MS = 0
private const val PULL_REFRESH_RESULT_EXIT_ANIMATION_DURATION_MS = 150
private const val PULL_REFRESH_RESULT_DISPLAY_DURATION_MS = 1100L

private class PullRefreshResultState {
    var text by mutableStateOf("")
    var isVisible by mutableStateOf(false)
    private var displayVersion = 0

    fun clear() {
        displayVersion += 1
        text = ""
        isVisible = false
    }

    fun showNow(resultText: String): Int {
        displayVersion += 1
        text = resultText
        isVisible = true
        return displayVersion
    }

    suspend fun hideAfterDelay(version: Int) {
        delay(PULL_REFRESH_RESULT_DISPLAY_DURATION_MS)
        if (version != displayVersion) {
            return
        }
        isVisible = false
        delay(PULL_REFRESH_RESULT_EXIT_ANIMATION_DURATION_MS.toLong())
        if (version == displayVersion) {
            text = ""
        }
    }

    suspend fun show(resultText: String) {
        val version = showNow(resultText)
        delay(PULL_REFRESH_RESULT_DISPLAY_DURATION_MS)
        if (version == displayVersion) {
            clear()
        }
    }
}

@Composable
private fun PullRefreshHeaderView(
    pullProgress: Float,
    isRefreshing: Boolean,
    refreshThreshold: Dp,
    refreshHeaderSkin: PullRefreshHeaderUiState,
    pullRefreshResultState: PullRefreshResultState,
    showResultInHeader: Boolean = true
) {
    val refreshingTextColor = ComposeColor(refreshHeaderSkin.refreshingTextColor)

    if (showResultInHeader && pullRefreshResultState.isVisible && pullRefreshResultState.text.isNotBlank()) {
        PullRefreshResultBar(refreshHeaderSkin, pullRefreshResultState)
        return
    }

    // 对齐 PullHeadView：lottie 缩放起始高度 = 文字高度估算 + DISTANCE(30dp)
    // 文字高度约 14sp ≈ 14dp，加上 DISTANCE 30dp，约 44dp
    // scaleStartHeight / refreshThreshold 即为缩放起始进度
    val lottieScaleStartProgress = remember(refreshThreshold) {
        val textHeightDp = 14f
        val distanceDp = PULL_LOTTIE_SCALE_START_OFFSET.value
        val scaleStartHeightDp = textHeightDp + distanceDp
        (scaleStartHeightDp / refreshThreshold.value).coerceIn(0f, 1f)
    }

    // 计算 lottie 缩放比例：在 [scaleStartProgress, 1.0] 区间内从 0 → 1
    val lottieScale = if (isRefreshing) {
        1f
    } else {
        if (pullProgress <= lottieScaleStartProgress) {
            0f
        } else {
            ((pullProgress - lottieScaleStartProgress) / (1f - lottieScaleStartProgress))
                .coerceIn(0f, 1f)
        }
    }

    // 文字状态对齐 PullHeadView
    val refreshText = when {
        isRefreshing -> "正在刷新"
        pullProgress >= 1f -> "释放刷新"
        else -> "下拉刷新"
    }

    val shouldShowHeader = isRefreshing || pullProgress > 0f
    if (shouldShowHeader) {
        val lottieFileName = refreshHeaderSkin.lottieUrl.takeIfNotBlank()
            ?: Lotties.rotatingPullToRefresh
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(refreshThreshold),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                // lottie 动画：刷新中自动循环播放，否则停在第0帧并按进度缩放
                QnLottie(
                    modifier = Modifier
                        .height(PULL_LOTTIE_HEIGHT)
                        .width(PULL_LOTTIE_HEIGHT * lottieScale),
                    fileName = lottieFileName,
                    tag = "pullToRefresh",
                    autoPlay = isRefreshing,
                    infinity = isRefreshing,
                    // 非刷新状态固定在第0帧；刷新中不控制进度（NaN），让动画自由播放
                    progress = if (isRefreshing) Float.NaN else 0f,
                )

                QnText(
                    text = refreshText,
                    color = refreshingTextColor,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        Spacer(modifier = Modifier.height(0.dp))
    }

}

@Composable
private fun PullRefreshResultBar(
    refreshHeaderSkin: PullRefreshHeaderUiState,
    pullRefreshResultState: PullRefreshResultState,
    height: Dp = PULL_REFRESH_RESULT_HEIGHT
) {
    val refreshedBgColor = ComposeColor(refreshHeaderSkin.refreshedBgColor)
    val refreshedTextColor = ComposeColor(refreshHeaderSkin.refreshedTextColor)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(refreshedBgColor),
        contentAlignment = Alignment.Center
    ) {
        QnText(
            text = pullRefreshResultState.text,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = refreshedTextColor,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
