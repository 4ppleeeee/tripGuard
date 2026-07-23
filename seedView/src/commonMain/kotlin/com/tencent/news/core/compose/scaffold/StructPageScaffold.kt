@file:Suppress("MatchingDeclarationName", "FunctionNaming", "RedundantConstructorKeyword")

package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.interaction.MutableInteractionSource
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.BoxWithConstraints
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyListScope
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.alpha
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.kuikly.core.views.IScrollerViewEventObserver
import com.tencent.kuikly.core.views.ScrollParams
import com.tencent.kuikly.core.views.ScrollerAttr
import com.tencent.kuikly.core.views.ScrollerEvent
import com.tencent.kuikly.core.views.ScrollerView
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.compose.platform.fdp
import com.tencent.news.core.compose.platform.pageViewHeight
import com.tencent.news.core.compose.platform.pageViewWidth
import com.tencent.news.core.compose.platform.suitableScaleRatio
import com.tencent.news.core.compose.platform.sysDensity
import com.tencent.news.core.compose.scaffold.modifiers.onSizeChangedDp2
import com.tencent.news.core.compose.scaffold.modifiers.scrollsToTop
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatus
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatusInner
import com.tencent.news.core.compose.scaffold.registry.LocalStructContentVideoListState
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.registry.LocalStructRootListState
import com.tencent.news.core.compose.scaffold.registry.LocalStructSelectedListState
import com.tencent.news.core.compose.scaffold.registry.LocalStructTitleAreaHeight
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.utils.ComposeUtils
import com.tencent.news.core.compose.view.list.IQnListState
import com.tencent.news.core.compose.view.list.LazyListStateAdapter
import com.tencent.news.core.compose.view.list.scrollToBottom
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.IStructHeaderAware
import com.tencent.news.core.page.model.PagerWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.constants.INVALID_NUM
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

internal typealias TitleBarImpl = @Composable () -> Unit
internal typealias BottomBarImpl = @Composable () -> Unit
internal typealias HeaderImpl = @Composable () -> Unit
internal typealias HangingViewImpl = @Composable (() -> Unit)
internal typealias TitleHangingViewImpl = @Composable (() -> Unit)
internal typealias ChannelBarImpl = @Composable (() -> Unit)
internal typealias MainContentImpl = @Composable (StructPageScrollScaffold) -> Unit
internal typealias LayerImpl = @Composable BoxScope.() -> Unit
internal typealias BgImpl = @Composable BoxScope.() -> Unit

@KmmInternalApi // 不要直接使用基础组件，要用 StructComposePage
@Composable
internal fun StructPageScaffold(
    modifier: Modifier = Modifier,
    pageWidget: StructPageWidget2,
    enableRootPullRefresh: Boolean = false,

    // 页面UI组件：
    titleBar: TitleBarImpl?,
    bottomBar: BottomBarImpl?,
    header: HeaderImpl?,
    hangingView: HangingViewImpl? = null,
    titleHangingView: TitleHangingViewImpl? = null,
    channelBar: ChannelBarImpl? = null,
    mainContent: MainContentImpl,
    layerView: LayerImpl? = null,
    bgView: BgImpl? = null,
) {
    BoxWithConstraints {
        // 页面高度变化时主动重建一下各种滚动state；否则某些机型上，listState记录的offset会错位，导致联动问题
        val density = LocalDensity.current.density
        val pageEnv = key(constraints.maxHeight) {
            // Kuikly的maxHeightDp会慢一拍，所以这里自己计算下
            val listHeight = (constraints.maxHeight / density).dp
            rememberStructPageEnv(pageWidget, listHeight)
        }

        StructPageServiceBox(modifier, pageEnv) {
            if (bgView != null) {
                bgView()
            }

            Column {
                // 【体验优化】：只有用这种 Column 结构时，才能保证页面首次进入无内容高度闪动；
                // 因为 titleBarHeight 依赖 state 重组，会慢一拍，不使用 Column 而监听 state，会有极短的一个抖动
                if (pageWidget.fixTitleBarAboveContent()) {
                    BuildTitleArea(pageEnv, titleBar, channelBar)
                }

                StructRootColumn(pageEnv, enableRootPullRefresh) {
                    // Header区域（组件注册：StructHeaderRegistry）
                    // 【注意】没有Header时候要隐藏item布局，否则有item但是高度为0，下面的悬停Hover组件有bug，会乱跳
                    if (pageWidget.canShowHeaderArea()) {
                        item {
                            BuildHeaderArea(pageEnv, header)
                        }
                    }

                    // 将hangingView和列表内容包在一起，实现重叠布局
                    item {
                        // 【体验优化】这里用box也能实现，但hanging高度计算会慢半拍，页面有抖动
                        // 用 column 的话，在layout阶段直接布局好，不需要等state刷新
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .height(pageEnv.pageSize.getContainerHeight())
                                .debugBg(Color.Yellow)
                        ) {
                            BuildColumnHoverArea(pageEnv, hangingView, channelBar)

                            BuildContentArea(pageEnv, mainContent)
                        }
                    }
                }
            }

            // 顶部导航栏 TitleBar
            if (!pageWidget.fixTitleBarAboveContent()) {
                BuildTitleArea(pageEnv, titleBar, channelBar)
            }

            // titleHanging区域：浮层形式，位于titleBar下方，默认隐藏，Header折叠后展示
            BuildTitleHangingArea(pageEnv, titleHangingView)

            // 底部导航
            BuildBottomBarArea(pageEnv, bottomBar)

            // 浮层挂件/弹窗
            BuildLayerArea(layerView)
        }
    }
}

// 顶部圆角，单位dp（切左上和右上2处，同时整体内容区向上偏移遮盖）
private fun PagerWidget?.buildTopRadius(): Modifier {
    val topRadius = this?.action?.topRadius?.takeIf { it > 0 }
    if (topRadius == null) {
        return Modifier
    }
    return Modifier.offset(y = (-topRadius).dp)
        .clip(RoundedCornerShape(topStart = topRadius.dp, topEnd = topRadius.dp))
}

// 绑定页面基础服务provider的根布局box
@OptIn(KmmInternalApi::class)
@Composable
private fun StructPageServiceBox(
    modifier: Modifier,
    pageEnv: StructPageScaffoldEnv,
    content: @Composable BoxScope.() -> Unit
) {
    val pageWidget = pageEnv.pageWidget
    val pageListState = pageEnv.pageListState
    val pageSize = pageEnv.pageSize

    // 内部使用的header折叠态：不受 fixTitleBarAboveContent 影响
    val headerCollapseStateInner = pageWidget.rememberHeaderCollapseState(pageListState, pageSize)

    // 对外给TitleBar和各种Btn回调的折叠态
    val headerCollapseState = remember(headerCollapseStateInner) {
        derivedStateOf {
            pageWidget.fixTitleBarAboveContent() || headerCollapseStateInner.value
        }
    }

    val listVideoState = remember { ListVideoState() }

    // 屏幕尺寸变动时，校准header折叠态：
    if (!pageWidget.fixTitleBarAboveContent()) {
        RecoveryHeaderCollapseForScreenChange(headerCollapseState, pageListState)
    }

    val titleAreaHeight = remember {
        derivedStateOf {
            pageSize.titleBarHeight + pageSize.hangingHeight
        }
    }

    CompositionLocalProvider(
        LocalStructContentVideoListState provides listVideoState,
        LocalStructRootListState provides pageListState.rootListState,
        LocalStructSelectedListState provides pageListState.selectedListState,
        LocalHeaderCollapseStatus provides headerCollapseState,
        LocalHeaderCollapseStatusInner provides headerCollapseStateInner,
        LocalStructTitleAreaHeight provides titleAreaHeight,
    ) {
        // 兼容：宽高变化之后不刷新UI的问题，需要手动指定下宽高
        Box(modifier = modifier.width(pageSize.pageViewWidth).height(pageSize.pageViewHeight)) {
            content()
        }
    }
}

@Composable
private fun RecoveryHeaderCollapseForScreenChange(
    headerCollapseState: State<Boolean>,
    pageListState: StructPageListState,
) {
    val enable = remember { getShiplySwitch("enable_compose_header_recovery", true) }
    if (!enable) {
        return
    }

    LaunchedEffect(pageViewWidth(), pageViewHeight()) {
        val mainListState = pageListState.selectedListState.value

        // 如果子列表已经滑动了，header保持折叠
        val alreadyCollapsed = mainListState != null && mainListState.contentOffset > 0 ||
                headerCollapseState.value

        if (alreadyCollapsed) {
            pageListState.rootListState.scrollToBottom()
        }
    }
}

// 整个页面根列表
@Composable
private fun StructRootColumn(
    pageEnv: StructPageScaffoldEnv,
    enablePullRefresh: Boolean = false,
    content: LazyListScope.() -> Unit
) {
    val pageWidget = pageEnv.pageWidget
    val pageListState = pageEnv.pageListState
    val pageSize = pageEnv.pageSize

    val contentHeight = pageSize.getContentHeight(pageWidget.fixTitleBarAboveContent())

    if (enablePullRefresh) {
        val refreshState = rememberRootPullRefreshState(pageListState.rootListState)
        RootPullRefreshEffects(refreshState)

        Box {
            LazyColumn(
                modifier = Modifier.width(pageSize.pageViewWidth).debugBg(Color.Blue)
                    .height(contentHeight)
                    .bouncesEnable(true)
                    .scrollsToTop(true)
                    .nativeRef { _ ->
                        @Suppress("UNCHECKED_CAST")
                        val scrollerView = this as? ScrollerView<ScrollerAttr, ScrollerEvent>
                        if (scrollerView != null) {
                            scrollerView.setContentInset(top = 0f, animated = false)
                            scrollerView.setContentInsetWhenEndDrag(top = 0f)
                            refreshState.scrollerViewRef = scrollerView
                        }
                    },
                state = pageListState.rootListState.realListState,
                content = content
            )

            // 点击穿透拦截层：下拉或刷新过程中，native ScrollerView 通过 contentInset
            // 使列表视觉整体下移，但 Compose 层记录的 item 点击区域仍在原位置，
            // 若不拦截，点击视觉空白处会命中下方 item（例如跳转到下一个作品详情页）。
            // 仅在 isRefreshing 或 pullProgress > 0f 时覆盖，正常情况下不影响交互。
            if (refreshState.isRefreshing || refreshState.pullProgress > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                )
            }

            // Pull refresh indicator overlay
            RootPullRefreshIndicator(refreshState)
        }
    } else {
        LazyColumn(
            modifier = Modifier.width(pageSize.pageViewWidth).debugBg(Color.Blue)
                .height(contentHeight)
                .bouncesEnable(false)
                .scrollsToTop(true),
            state = pageListState.rootListState.realListState,
            content = content
        )
    }
}

// ════════════════════════════════════════════════════════════════
// Root pull-to-refresh: state, effects, indicator
// ════════════════════════════════════════════════════════════════

private const val ROOT_REFRESH_THRESHOLD_DP = 80f

/**
 * 根列表下拉刷新状态容器
 */
@Stable
private class RootPullRefreshState(
    val rootListState: LazyListStateAdapter,
) {
    var isRefreshing by mutableStateOf(false)
    var pullProgress by mutableStateOf(0f)
    var scrollerViewRef by mutableStateOf<ScrollerView<ScrollerAttr, ScrollerEvent>?>(null)
    var nativeContentOffsetY by mutableStateOf(0f)
    var dragStartOffsetY = 0f
    var isDraggingFromTop = false

    val refreshThreshold = ROOT_REFRESH_THRESHOLD_DP.fdp
    val refreshThresholdDp get() = ROOT_REFRESH_THRESHOLD_DP

    fun isAtTop(): Boolean {
        val firstIndex = rootListState.firstVisibleItemIndex
        val firstOffset = rootListState.firstVisibleItemScrollOffset
        return firstIndex == 0 && firstOffset <= 0
    }
}

@Composable
private fun rememberRootPullRefreshState(
    rootListState: LazyListStateAdapter,
): RootPullRefreshState {
    return remember(rootListState) {
        RootPullRefreshState(rootListState)
    }
}

/**
 * 根列表下拉刷新副作用管理：
 * 注册 ScrollerView observer，监听拖拽事件，松手时触发刷新
 */
@Composable
private fun RootPullRefreshEffects(state: RootPullRefreshState) {
    val coroutineScope = rememberCoroutineScope()
    val pageVM = LocalStructPageViewModel.current

    if (state.scrollerViewRef != null) {
        DisposableEffect(state.scrollerViewRef) {
            val scrollerView = state.scrollerViewRef ?: return@DisposableEffect onDispose { }
            scrollerView.setContentInset(top = 0f, animated = false)
            scrollerView.setContentInsetWhenEndDrag(top = 0f)
            val observer = object : IScrollerViewEventObserver {
                override fun onContentOffsetDidChanged(
                    offsetX: Float,
                    offsetY: Float,
                    params: ScrollParams
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
                            animated = true
                        )

                        coroutineScope.launch {
                            try {
                                pageVM?.refresh(
                                    FeedsRefreshRequest(
                                        refreshForward = ListRefreshForward.TOP_REFRESH,
                                        refreshAction = ListRefreshAction.PULL_DOWN
                                    )
                                )
                                // Ensure the refresh indicator stays visible for at least
                                // a short duration, then wait for the data to settle.
                                delay(1500)
                            } finally {
                                finishRootPullRefresh(state, scrollerView, animated = true)
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
                finishRootPullRefresh(state, scrollerView, animated = false)
            }
        }
    }

    // Dynamically set inset-when-end-drag based on pull progress
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

    LaunchedEffect(Unit) {
        var wasRefreshing = false
        snapshotFlow { state.isRefreshing }.collect { isRefreshing ->
            if (wasRefreshing && !isRefreshing) {
                val scrollerView = state.scrollerViewRef ?: return@collect
                finishRootPullRefresh(state, scrollerView, animated = true)
                delay(300)
                state.pullProgress = 0f
            }
            wasRefreshing = isRefreshing
        }
    }
}

private fun finishRootPullRefresh(
    state: RootPullRefreshState,
    scrollerView: ScrollerView<ScrollerAttr, ScrollerEvent>?,
    animated: Boolean,
) {
    scrollerView?.setContentInset(top = 0f, animated = animated)
    scrollerView?.setContentInsetWhenEndDrag(top = 0f)
    state.isRefreshing = false
    state.pullProgress = 0f
    state.isDraggingFromTop = false
}

/**
 * 根列表下拉刷新指示器浮层：使用刷新 lottie 默认颜色
 *
 * iOS/鸿蒙的 ScrollerView 表现与安卓不一致：
 * - 鸿蒙：松手时 ScrollerView 会自动将 contentInset 设置为 0，导致浮层被隐藏
 * - iOS：页面首次加载时 contentOffset 可能为非零值，导致指示器错误显示
 * 参考 StaggeredPullRefreshIndicator 中的平台特殊处理
 */
@Composable
private fun RootPullRefreshIndicator(state: RootPullRefreshState) {
    // When not refreshing and user is not pulling, force hide the indicator.
    // This prevents iOS from showing the indicator on page entry due to
    // initial contentOffset anomalies from UIScrollView bounce behavior.
    val indicatorOffsetY = if (!state.isRefreshing && state.pullProgress <= 0f) {
        -state.refreshThreshold
    } else when {
        (isHarmonyPlatform() || isIOSPlatform()) && state.isRefreshing -> {
            if (state.nativeContentOffsetY < 0f) {
                (-state.refreshThreshold + state.nativeContentOffsetY.unaryMinus().fdp)
                    .coerceAtLeast(0.dp)
            } else {
                -(state.nativeContentOffsetY.coerceAtMost(state.refreshThresholdDp)).fdp
            }
        }

        else -> {
            -state.refreshThreshold + state.nativeContentOffsetY.coerceAtMost(0f).unaryMinus().fdp
        }
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
            showText = false,
            foregroundColor = null,
            lottieTintColor = null
        )
    }
}

@Composable
private fun BuildTitleArea(
    pageEnv: StructPageScaffoldEnv,
    titleBar: TitleBarImpl?,
    channelBar: ChannelBarImpl?,
) {
    val fixChannelBarBelowTitleBar = pageEnv.pageWidget.pageConfig.fixChannelBarBelowTitleBar
    val showTitleBarArea = pageEnv.pageWidget.canShowTitleBarArea()

    if (!showTitleBarArea) {
        return
    }

    val clearScreen by pageEnv.pageWidget.findStructPageVM()?.pageUiConfig?.clearScreen
        ?.collectAsState() ?: remember { mutableStateOf(false) }
    val alpha = if (clearScreen) 0f else 1f

    Column(
        Modifier.fillMaxWidth().wrapContentHeight()
            .alpha(alpha)
            .onSizeChangedDp2 {
                pageEnv.pageSize.updateTitleBarHeight(it.height)
            }
    ) {
        if (titleBar != null) {
            titleBar()      // 组件注册：StructTitleBarRegistry
        }
        if (fixChannelBarBelowTitleBar && channelBar != null) {
            channelBar()    // 组件注册：StructChannelBarRegistry
        }
    }
}

@OptIn(KmmInternalApi::class)
@Composable
private fun BoxScope.BuildTitleHangingArea(
    pageEnv: StructPageScaffoldEnv,
    titleHangingView: TitleHangingViewImpl?
) {
    titleHangingView ?: return

    val isHeaderCollapsed by LocalHeaderCollapseStatusInner.current

    // 默认隐藏，当Header折叠后展示；浮层形式不占据Column布局空间
    if (isHeaderCollapsed) {
        Column(
            modifier = Modifier
                .fillMaxWidth().wrapContentHeight()
                .align(Alignment.TopStart)
                .padding(top = pageEnv.pageSize.titleBarHeight)
        ) {
            titleHangingView()  // 组件注册：StructHangingRegistry
        }
    }

    // header折叠状态，可以派发给vm
    val titleHangingVM = pageEnv.pageWidget.titleHanging?.asWidgetVM
    if (titleHangingVM is IStructHeaderAware) {
        LaunchedEffect(titleHangingVM, isHeaderCollapsed) {
            titleHangingVM.onHeaderCollapseChanged(isHeaderCollapsed)
        }
    }
}

@Composable
private fun BuildHeaderArea(
    pageEnv: StructPageScaffoldEnv,
    header: HeaderImpl?
) {
    header ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth().wrapContentHeight().debugBg(Color.Red)
            .onSizeChangedDp2 {
                pageEnv.pageSize.updateHeaderHeight(it.height)

                val offset = pageEnv.pageWidget.pager?.action?.topRadius ?: 0f
                pageEnv.pageSize.updatePagerOffset(offset.dp)
            }
    ) {
        // 顶部header
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            header()
        }
    }
}

@Composable
private fun BuildContentArea(
    pageEnv: StructPageScaffoldEnv,
    mainContent: MainContentImpl,
    modifier: Modifier = Modifier
) {
    val scrollScaffold = pageEnv.rememberScrollScaffold()
    Box(
        modifier = modifier.fillMaxWidth()
            .height(pageEnv.pageSize.getListHeight()) // 必须限定内容区高度，用fillMax布局会溢出crash
            .debugBg(Color.Blue.copy(alpha = 0.3f))
    ) {
        // 列表内容：支持多tab（组件注册：StructChannelRegistry）
        mainContent(scrollScaffold)
    }
}

@Composable
private fun BuildColumnHoverArea(
    pageEnv: StructPageScaffoldEnv,
    hangingView: HangingViewImpl?,
    channelBar: ChannelBarImpl?,
) {
    // 检查是否有hanging内容需要测量
    val fixChannelBarBelowTitleBar = pageEnv.pageWidget.pageConfig.fixChannelBarBelowTitleBar
    val showChannelBarInHanging = !fixChannelBarBelowTitleBar && channelBar != null
    val hasHangingContent = showChannelBarInHanging || hangingView != null

    // 底层：悬停组件（固定在顶部）
    if (hasHangingContent) {
        val clearScreen by pageEnv.pageWidget.findStructPageVM()?.pageUiConfig?.clearScreen
            ?.collectAsState() ?: remember { mutableStateOf(false) }
        val hoverAlpha = if (clearScreen) 0f else 1f

        Column(
            modifier = Modifier
                .fillMaxWidth().wrapContentHeight()
                .alpha(hoverAlpha)
                .debugBg(Color.Green)
                .then(pageEnv.pageWidget.pager.buildTopRadius())
                .onSizeChangedDp2 {
                    pageEnv.pageSize.updateHangingHeight(it.height)
                }
        ) {
            if (showChannelBarInHanging && channelBar != null) {
                channelBar()
            }
            if (hangingView != null) {
                hangingView()
            }
        }
    }
}

@Composable
private fun BoxScope.BuildBottomBarArea(
    pageEnv: StructPageScaffoldEnv,
    bottomBar: BottomBarImpl?
) {
    val coverBottomSafeAreaBar = pageEnv.pageWidget.pageConfig.coverBottomSafeAreaBar
    if (coverBottomSafeAreaBar) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ComposeUtils.rememberSafeAreaBottomHeight().fdp)
                .align(Alignment.BottomStart)
                .background(QnColor.bgPage)
        )
    }

    bottomBar ?: return

    // iOS全面屏适配：
    val addBottomSafeAreaPadding = !pageEnv.pageWidget.pageConfig.expandBottomSafeAreaForPage
    val bottomBarHeightToEscape: Dp = if (addBottomSafeAreaPadding) {
        ComposeUtils.rememberSafeAreaBottomHeight().fdp
    } else {
        0.dp
    }
    Box(
        modifier = Modifier
            .fillMaxWidth().wrapContentHeight()
            .align(Alignment.BottomStart)
            .padding(bottom = bottomBarHeightToEscape)
            .onSizeChangedDp2 {
                val targetHeight = it.height + bottomBarHeightToEscape
                pageEnv.pageSize.updateBottomBarHeight(targetHeight)
            }
    ) {
        // 顶部导航栏 BottomBar（组件注册：StructBottomBarRegistry）
        bottomBar()
    }
}

@Composable
private fun BoxScope.BuildLayerArea(layerView: LayerImpl?) {
    layerView ?: return

    Box(Modifier.align(Alignment.BottomEnd)) { // 挂件默认都右下角对齐
        // 全屏浮层挂件（组件注册：StructLayerRegistry）
        layerView()
    }
}

@Composable
private fun StructPageWidget2.rememberHeaderCollapseState(
    pageListState: StructPageListState,
    pageSize: StructPageSize
): State<Boolean> {
    val rootListState = pageListState.rootListState
    val collapseRatio = getTitleBarCollapseRatio()
    val initCollapse = header?.initCollapse ?: false

    // 监听用户是否滑动过列表
    var hasListScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(rootListState) {
        snapshotFlow { rootListState.isScrollInProgress }
            .collectLatest { isScrolling ->
                if (isScrolling && !hasListScrolled) {
                    hasListScrolled = true
                }
            }
    }

    val density = LocalDensity.current

    // 重要，一定要加上pageSize为key，否则折叠屏、转屏等操作，尺寸计算不对
    return remember(collapseRatio, rootListState, pageSize, density) {
        derivedStateOf {
            if (!hasListScrolled && initCollapse) {
                return@derivedStateOf true // 初始时保持折叠台，等用户手动滑动后再放开判断
            }

            // 第一个item是header，第二个是content(lazyColumn)，content可见时，header已经隐藏了
            if (rootListState.firstVisibleItemIndex > 0) {
                return@derivedStateOf true
            }

            // 计算header可见高度
            val visibleHeaderHeight = pageSize.getVisibleHeaderHeight()
            if (visibleHeaderHeight <= 0.dp) {
                return@derivedStateOf false
            }

            // 【注意】kuikly有个bug：rootListState.setSelection之后，contentOffset会不准
            // 改为根据第一个cell判断滚动距离
            val offsetPx = rootListState.layoutInfo.firstItemOffset
            val offsetDp = with(density) { abs(offsetPx).toDp() }
            val collapsedRatio = offsetDp / visibleHeaderHeight
            return@derivedStateOf collapsedRatio >= collapseRatio
        }
    }
}

@Stable
private class StructPageScaffoldEnv(
    val pageWidget: StructPageWidget2,
    val pageListState: StructPageListState,
    val pageSize: StructPageSize
)

@Composable
private fun rememberStructPageEnv(
    pageWidget: StructPageWidget2,
    maxHeight: Dp
): StructPageScaffoldEnv {
    // 品字形页面，整体的listState：
    val pageListState = pageWidget.rememberPageListState()

    // 各种组件尺寸：
    val pageSize = rememberStructPageSize(maxHeight)
    pageSize.contentFullScreen = pageWidget.pageConfig.contentFullScreen

    return remember(pageListState, pageSize) {
        StructPageScaffoldEnv(pageWidget, pageListState, pageSize)
    }
}

// 品字形页面的列表状态管理
@Stable
private class StructPageListState(
    val rootListState: LazyListStateAdapter,
    val selectedListState: MutableState<IQnListState?>
)

@Composable
private fun StructPageWidget2.rememberPageListState(): StructPageListState {
    val showHeaderArea = canShowHeaderArea()
    val initCollapse: Boolean = header?.initCollapse ?: false
    val rootInitIndex = if (initCollapse && showHeaderArea) 1 else 0

    val listState = rememberLazyListState(rootInitIndex)
    val rootListState = remember { LazyListStateAdapter(listState) }
    val selectedListState = remember { mutableStateOf<IQnListState?>(null) }
    return remember(rootListState, selectedListState) {
        StructPageListState(rootListState, selectedListState)
    }
}

@Composable
private fun StructPageScaffoldEnv.rememberScrollScaffold(): StructPageScrollScaffold {
    val rootListState = pageListState.rootListState
    val selectedListState = pageListState.selectedListState
    val listHeight = pageSize.getListHeight()
    val pageHeight = pageSize.pageViewHeight

    LaunchedEffect(listHeight, pageSize.hangingHeight) {
        NewsChannelLog.debug("Page") {
            "品字形 ListHeight：${listHeight}, hangingHeight: ${pageSize.hangingHeight}"
        }
    }

    return remember(rootListState, selectedListState, listHeight, pageHeight) {
        StructPageScrollScaffold(
            rootListState = rootListState,
            selectedListState = selectedListState,
            listHeight = listHeight,
            pageHeight = pageHeight
        )
    }
}

// 品字形页面的尺寸状态管理
@Stable
private class StructPageSize {
    var bottomBarHeight: Dp by mutableStateOf(0.dp) // 底部栏高度（包含安全区域）
        private set

    var headerHeight: Dp by mutableStateOf(0.dp)    // Header区域高度（包含悬停组件）
        private set

    var titleBarHeight: Dp by mutableStateOf(0.dp)  // 标题栏高度
        private set

    var hangingHeight: Dp by mutableStateOf(0.dp)   // 悬停组件高度（ChannelBar + HangingView）
        private set

    var pageViewWidth: Dp by mutableStateOf(0.dp)   // 页面视图宽度
        private set

    var pageViewHeight: Dp by mutableStateOf(0.dp)  // 页面视图高度
        private set

    var pagerOffset: Dp by mutableStateOf(0.dp)
        private set

    var contentFullScreen: Boolean = false

    /**
     * 计算内容区域高度（考虑TitleBar是否固定）
     * @param fixTitleBarAboveContent TitleBar是否固定在内容上方
     */
    fun getContentHeight(fixTitleBarAboveContent: Boolean): Dp {
        return if (fixTitleBarAboveContent) {
            pageViewHeight - titleBarHeight - bottomBarHeight
        } else {
            pageViewHeight - bottomBarHeight
        }
    }

    /**
     * 计算列表可用高度
     */
    fun getListHeight(): Dp = getContainerHeight() - hangingHeight

    /**
     * 计算Header可见高度（用于折叠计算）
     */
    fun getVisibleHeaderHeight(): Dp = headerHeight - titleBarHeight - pagerOffset // 有offset时，提前折叠

    /**
     * 计算容器高度（不受hangingView高度变化影响）
     */
    fun getContainerHeight(): Dp = if (!contentFullScreen) {
        pageViewHeight - (titleBarHeight + bottomBarHeight)
    } else {
        pageViewHeight - bottomBarHeight
    } - pagerOffset // 有offset时，可展示内容区减小

    fun updateBottomBarHeight(height: Dp) {
        if (bottomBarHeight != height) {
            bottomBarHeight = height
            NewsChannelLog.debug("Page") { "品字形 BottomBar 高度：${height}" }
        }
    }

    fun updateHeaderHeight(height: Dp) {
        if (headerHeight != height) {
            headerHeight = height
            NewsChannelLog.debug("Page") { "品字形 Header 高度：${height}" }
        }
    }

    fun updateTitleBarHeight(height: Dp) {
        if (titleBarHeight != height) {
            titleBarHeight = height
            NewsChannelLog.debug("Page") { "品字形 TitleBar 高度：${height}" }
        }
    }

    fun updateHangingHeight(height: Dp) {
        if (hangingHeight != height) {
            hangingHeight = height
            NewsChannelLog.debug("Page") { "品字形 hanging 高度：${height}" }
        }
    }

    fun updatePagerOffset(offset: Dp) {
        if (pagerOffset != offset) {
            pagerOffset = offset
            NewsChannelLog.debug("Page") { "品字形 Pager Offset：${offset}" }
        }
    }

    fun updatePageSize(width: Dp, height: Dp) {
        pageViewWidth = width
        pageViewHeight = height
        NewsChannelLog.debug("Page") { "品字形 页面宽高：${width}/${height}" }
    }
}

@Composable
private fun rememberStructPageSize(maxHeight: Dp): StructPageSize {
    val pageWidth = pageViewWidth()

    // 注意：当compose子tab再次嵌套时，maxHeight会比pageViewHeight小，不能直接取屏幕高
    return remember(pageWidth, maxHeight) {
        StructPageSize().apply {
            updatePageSize(pageWidth, maxHeight)
        }
    }
}

@Composable
internal fun WatchPageScrollFlow(
    scrollScaffold: StructPageScrollScaffold,
    displayItems: List<IKmmFeedsItem>?,
) {
    val pageScrollState = LocalStructPageViewModel.current?.scrollStateFlow
        ?: return
    // 用 rememberUpdatedState 持有 displayItems 最新值，避免将其作为 LaunchedEffect key
    // 若将 displayItems 作为 key，数据更新时协程重启会导致滚顶等事件丢失（SharedFlow 无 replay）
    val currentDisplayItems by rememberUpdatedState(displayItems)
    LaunchedEffect(scrollScaffold, pageScrollState) {
        pageScrollState.collectLatest { scrollState ->
            if (scrollState.scrollToTop) {
                scrollScaffold.scrollToTop(scrollState.animate)
                return@collectLatest
            }
            if (scrollState.scrollToBottom) {
                scrollScaffold.scrollToBottom(scrollState.animate)
                return@collectLatest
            }
            if (scrollState.scrollChannelBarToTop) {
                scrollScaffold.collapseHeader(scrollState.animate)
                return@collectLatest
            }
            if (scrollState.fixBottomScollState) {
                scrollScaffold.fixBottomScrollState()
                return@collectLatest
            }
            if (scrollState.scrollByOffsetY != 0f) {
                scrollScaffold.scrollBy(scrollState.scrollByOffsetY)
                return@collectLatest
            }
            if (scrollState.scrollToPosition >= 0) {
                scrollScaffold.scrollToIndex(
                    index = scrollState.scrollToPosition,
                    animated = scrollState.animate,
                    scrollRootForIndex = scrollState.scrollRootHeader
                )
                return@collectLatest
            }

            // 有Item，滑动到对应Cell
            val currentItem = scrollState.currentItem
                ?: return@collectLatest
            val scrollIndex = currentDisplayItems?.indexOfFirst {
                it.flexDto.idStr == currentItem.flexDto.idStr
            } ?: INVALID_NUM
            // 调试日志：跟踪 MineProfile「刚刚看过」浮层点击后 scroll 消费链路。
            // 若 VM 侧打出 `scrollToIndexLogged[*]: emit=true` 但此行未打印，说明
            // LaunchedEffect 的 collector 断连或 displayItems 来自错误的 pageVM；
            // 若此行打出但 scrollIndex=-1，说明 emit 跑在 displayItems 更新之前；
            // 若 scrollIndex>=0 但 UI 仍不滚，锁定 `selectedListState.value == null`。
            println(
                "[WatchPageScrollFlow] consume: scrollIndex=$scrollIndex, " +
                    "displaySize=${currentDisplayItems?.size}, " +
                    "targetIdStr=${currentItem.flexDto.idStr}"
            )
            if (scrollIndex >= 0) {
                scrollScaffold.scrollToIndex(
                    index = scrollIndex,
                    animated = scrollState.animate,
                    scrollRootForIndex = scrollState.scrollRootHeader
                )
            }
        }
    }
}

private fun Modifier.debugBg(color: Color): Modifier {
    val debugScaffold = false // 需要调试时候，手动打开

    return if (isDebug() && debugScaffold) {
        this.background(color)
    } else {
        this
    }
}
