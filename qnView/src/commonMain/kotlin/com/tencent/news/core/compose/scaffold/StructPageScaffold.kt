@file:Suppress("MatchingDeclarationName", "FunctionNaming", "RedundantConstructorKeyword")

package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.defaultMinSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyListScope
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose_dsl.kuikly.extension.flingSpeedLimit
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.compose.adaptive.AdaptiveContent
import com.tencent.news.core.compose.platform.fdp
import com.tencent.news.core.compose.platform.pageViewHeight
import com.tencent.news.core.compose.platform.pageViewWidth
import com.tencent.news.core.compose.scaffold.modifiers.Hover
import com.tencent.news.core.compose.scaffold.modifiers.onSizeChangedDp2
import com.tencent.news.core.compose.scaffold.modifiers.scrollsToTop
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatus
import com.tencent.news.core.compose.scaffold.registry.LocalStructContentVideoListState
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.registry.LocalStructRootListState
import com.tencent.news.core.compose.scaffold.registry.LocalStructSelectedListState
import com.tencent.news.core.compose.view.list.IQnListState
import com.tencent.news.core.compose.view.list.LazyListStateAdapter
import com.tencent.news.core.compose.view.list.scrollToBottom
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.tads.constants.INVALID_NUM
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs
import com.tencent.news.core.compose.platform.safeAreaHeight
internal typealias TitleBarImpl = @Composable () -> Unit
internal typealias BottomBarImpl = @Composable () -> Unit
internal typealias HeaderImpl = @Composable () -> Unit
internal typealias HangingViewImpl = @Composable (() -> Unit)
internal typealias ChannelBarImpl = @Composable (() -> Unit)
internal typealias MainContentImpl = @Composable (StructPageScrollScaffold) -> Unit
internal typealias LayerImpl = @Composable BoxScope.() -> Unit
internal typealias BgImpl = @Composable BoxScope.() -> Unit

@KmmInternalApi // 不要直接使用基础组件，要用 StructComposePage
@Composable
internal fun StructPageScaffold(
    modifier: Modifier = Modifier,
    pageWidget: StructPageWidget2,

    // 页面UI组件：
    titleBar: TitleBarImpl?,
    bottomBar: BottomBarImpl?,
    header: HeaderImpl?,
    hangingView: HangingViewImpl? = null,
    channelBar: ChannelBarImpl? = null,
    mainContent: MainContentImpl,
    layerView: LayerImpl? = null,
    bgView: BgImpl? = null,
) {
    // 页面高度变化时不再重建state，避免鸿蒙PC拖动窗口高频resize导致白屏
    // 尺寸变化后的滚动校准由 RecoveryHeaderCollapseForScreenChange 处理
    val pageEnv = rememberStructPageEnv(pageWidget)

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

            // 【Header 固定模式】对齐 Android 视频底层页：播放器 Header 固定在顶部不跟随列表滚动
            // 此分支将 Header 放在 LazyColumn 之外，独立占据空间，其下方才是可滚动列表
            val fixHeader = pageWidget.fixHeaderAboveContent()
            if (fixHeader) {
                BuildHeaderArea(pageEnv, header)
            }

            StructRootColumn(pageEnv) {
                // Header区域（组件注册：StructHeaderRegistry）
                // 【注意】没有Header时候要隐藏item布局，否则有item但是高度为0，下面的悬停Hover组件有bug，会乱跳
                // 【固定 Header 模式】此处不再把 Header 放到列表里，由外层 Column 承载
                if (!fixHeader && pageWidget.canShowHeaderArea()) {
                    item {
                        BuildHeaderArea(pageEnv, header)
                    }
                }

                val useNewHoverMode = pageWidget.pageConfig.useNewHoverMode
                if (useNewHoverMode) {
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
                } else {
                    BuildListHoverArea(pageEnv, hangingView, channelBar)

                    item {
                        BuildContentArea(pageEnv, mainContent)
                    }
                }
            }
        }

        // 顶部导航栏 TitleBar
        if (!pageWidget.fixTitleBarAboveContent()) {
            BuildTitleArea(pageEnv, titleBar, channelBar)
        }

        // 底部导航
        BuildBottomBarArea(pageEnv, bottomBar)

        // 浮层挂件/弹窗
        BuildLayerArea(layerView)
    }
}

// 绑定页面基础服务provider的根布局box
@Composable
private fun StructPageServiceBox(
    modifier: Modifier,
    pageEnv: StructPageScaffoldEnv,
    content: @Composable BoxScope.() -> Unit
) {
    val pageWidget = pageEnv.pageWidget
    val pageListState = pageEnv.pageListState
    val pageSize = pageEnv.pageSize

    val headerCollapseState = pageWidget.rememberHeaderCollapseState(pageListState, pageSize)
    val listVideoState = remember { ListVideoState() }

    // 屏幕尺寸变动时，校准header折叠态：
    // Header 固定模式下，Header 不再是 LazyColumn 的 item，无需 recovery
    if (!pageWidget.fixTitleBarAboveContent() && !pageWidget.fixHeaderAboveContent()) {
        RecoveryHeaderCollapseForScreenChange(headerCollapseState, pageListState)
    }
    CompositionLocalProvider(
        LocalStructContentVideoListState provides listVideoState,
        LocalStructRootListState provides pageListState.rootListState,
        LocalStructSelectedListState provides pageListState.selectedListState,
        LocalHeaderCollapseStatus provides headerCollapseState
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
        val rootListState = pageListState.rootListState
        val mainListState = pageListState.selectedListState.value

        // 如果子列表已经滑动了，header保持折叠
        val alreadyCollapsed = mainListState != null && mainListState.contentOffset > 0 ||
                headerCollapseState.value

        if (alreadyCollapsed) {
            rootListState.scrollToBottom()
        } else {
            // 尺寸变化后，无论当前offset是否为0，都需要重新scrollToItem
            // 强制native ScrollView刷新内部布局状态，避免横竖屏切换后header卡住不能滑动
            val currentIndex = rootListState.firstVisibleItemIndex
            val currentOffset = rootListState.firstVisibleItemScrollOffset
            rootListState.scrollToItem(currentIndex, currentOffset)
        }
    }
}

// 整个页面根列表
@Composable
private fun StructRootColumn(
    pageEnv: StructPageScaffoldEnv,
    content: LazyListScope.() -> Unit
) {
    val pageWidget = pageEnv.pageWidget
    val pageListState = pageEnv.pageListState
    val pageSize = pageEnv.pageSize

    val contentHeight = pageSize.getContentHeight(pageWidget.fixTitleBarAboveContent())
    var modifier = Modifier.width(pageSize.pageViewWidth).debugBg(Color.Blue)
            .height(contentHeight)
            .bouncesEnable(false)
            .scrollsToTop(true)
    pageWidget.pageConfig.flingSpeedLimit?.let {
        modifier = modifier.flingSpeedLimit(it)
    }
    LazyColumn(
        modifier = modifier,
        state = pageListState.rootListState.realListState,
        content = content
    )
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
    Column(
        Modifier.fillMaxWidth().wrapContentHeight()
            .onSizeChangedDp2 {
                pageEnv.pageSize.updateTitleBarHeight(it.height, pageEnv.pageTag)
            }
    ) {
        if (titleBar != null) {
            titleBar()      // 组件注册：StructTitleBarRegistry
        }
        if (fixChannelBarBelowTitleBar && channelBar != null) {
            AdaptiveContent {
                channelBar()    // 组件注册：StructChannelBarRegistry
            }
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
                pageEnv.pageSize.updateHeaderHeight(it.height, pageEnv.pageTag)
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
        Column(
            modifier = Modifier
                .fillMaxWidth().wrapContentHeight()
                .debugBg(Color.Green)
                .onSizeChangedDp2 {
                    pageEnv.pageSize.updateHangingHeight(it.height, pageEnv.pageTag)
                }
        ) {
            if (showChannelBarInHanging && channelBar != null) {
                AdaptiveContent {
                    channelBar()
                }
            }
            if (hangingView != null) {
                AdaptiveContent {
                    hangingView()
                }
            }
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.BuildListHoverArea(
    pageEnv: StructPageScaffoldEnv,
    hangingView: HangingViewImpl?,
    channelBar: ChannelBarImpl?,
) {
    val showChannelBarInHanging =
        !pageEnv.pageWidget.pageConfig.fixChannelBarBelowTitleBar && channelBar != null
    if (!showChannelBarInHanging && hangingView == null) {
        return
    }
    val hoverTopHeight = if (pageEnv.pageWidget.fixTitleBarAboveContent()) {
        0.dp
    } else if (pageEnv.pageWidget.fixHeaderAboveContent()) {
        // Header 固定模式下，Header 已在 LazyColumn 外部占据空间，
        // Hover 从列表自身顶部开始，无需额外下移
        0.dp
    } else {
        pageEnv.pageSize.titleBarHeight
    }
    Hover(
        modifier = Modifier.fillMaxWidth().wrapContentHeight()
        .defaultMinSize(minHeight = 1.dp) // 用于缓解compose列表头部跳动bug，如果出现高度为0的item会抖动
            .debugBg(Color.Green)
            .onSizeChangedDp2 {
                pageEnv.pageSize.updateHangingHeight(it.height, pageEnv.pageTag)
            },
        hoverMarginTop = hoverTopHeight,
        listState = pageEnv.pageListState.rootListState,
    ) { // 这是个Column布局
        if (showChannelBarInHanging && channelBar != null) {
            AdaptiveContent {
                channelBar()    // 组件注册：StructChannelBarRegistry
            }
        }
        if (hangingView != null) {
            AdaptiveContent {
                hangingView()   // 组件注册：StructHangingRegistry
            }
        }
    }
}

@Composable
private fun BoxScope.BuildBottomBarArea(
    pageEnv: StructPageScaffoldEnv,
    bottomBar: BottomBarImpl?
) {
    bottomBar ?: return

    // iOS全面屏适配：
    val addBottomSafeAreaPadding = !pageEnv.pageWidget.pageConfig.expandBottomSafeAreaForPage
    val bottomBarHeightToEscape: Dp = if (addBottomSafeAreaPadding) {
        safeAreaHeight().fdp
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
                pageEnv.pageSize.updateBottomBarHeight(targetHeight, pageEnv.pageTag)
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
    val fixTitleBarAboveContent = fixTitleBarAboveContent()
    val fixHeaderAboveContent = fixHeaderAboveContent()
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
            // Header 固定模式下，Header 不跟随列表滚动，也不存在折叠联动；
            // 视同始终"未折叠"，让依赖此状态的下游逻辑保持简单一致
            if (fixHeaderAboveContent) {
                return@derivedStateOf false
            }
            if (!hasListScrolled && initCollapse) {
                return@derivedStateOf true // 初始时保持折叠台，等用户手动滑动后再放开判断
            }
            if (fixTitleBarAboveContent) {
                return@derivedStateOf true
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
) {
    // 页面标签，用于日志区分，格式：channelKey(channelName)
    val pageTag: String by lazy {
        val info = pageWidget.pageConfig.defaultChannelInfo
        "${info.channelKey}(${info.channelName})"
    }
}

@Composable
private fun rememberStructPageEnv(pageWidget: StructPageWidget2): StructPageScaffoldEnv {
    // 品字形页面，整体的listState：
    val pageListState = pageWidget.rememberPageListState()

    // 页面标签，用于日志区分
    val info = pageWidget.pageConfig.defaultChannelInfo
    val pageTag = "${info.channelKey}(${info.channelName})"

    // 各种组件尺寸：
    val pageSize = rememberStructPageSize(pageTag)
    pageSize.maxPageHeight = pageWidget.pageConfig.maxPageHeight.dp
    if (pageSize.pageViewHeight > pageWidget.pageConfig.maxPageHeight.dp && pageWidget.pageConfig.maxPageHeight > 0f) {
        pageSize.updatePageSize(pageSize.pageViewWidth, pageSize.maxPageHeight, pageTag)
    }
    pageSize.contentFullScreen = pageWidget.pageConfig.contentFullScreen
    pageSize.fixHeaderAboveContent = pageWidget.fixHeaderAboveContent()

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
    val showHeaderArea = canShowHeaderArea() && !fixHeaderAboveContent()
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
            "[${pageTag}] 品字形 ListHeight：${listHeight}, hangingHeight: ${pageSize.hangingHeight}"
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
private class StructPageSize constructor() {
    var maxPageHeight: Dp = 0.dp

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

    var contentFullScreen: Boolean = false

    // 【Header 固定模式】Header 独立固定在内容区上方，不跟随列表滚动
    // 开启时，内容列表高度需要再减去 headerHeight
    var fixHeaderAboveContent: Boolean = false

    /**
     * 计算内容区域高度（考虑TitleBar是否固定）
     * @param fixTitleBarAboveContent TitleBar是否固定在内容上方
     */
    fun getContentHeight(fixTitleBarAboveContent: Boolean): Dp {
        val base = if (fixTitleBarAboveContent) {
            pageViewHeight - titleBarHeight - bottomBarHeight
        } else {
            pageViewHeight - bottomBarHeight
        }
        // Header 固定时，LazyColumn 需再扣掉 Header 占用的高度
        return if (fixHeaderAboveContent) base - headerHeight else base
    }

    /**
     * 计算列表可用高度
     */
    fun getListHeight(): Dp = getContainerHeight() - hangingHeight

    /**
     * 计算Header可见高度（用于折叠计算）
     */
    fun getVisibleHeaderHeight(): Dp = headerHeight - titleBarHeight

    /**
     * 计算容器高度（不受hangingView高度变化影响）
     */
    fun getContainerHeight(): Dp {
        val base = if (!contentFullScreen) {
            pageViewHeight - (titleBarHeight + bottomBarHeight)
        } else {
            pageViewHeight - bottomBarHeight
        }
        return if (fixHeaderAboveContent) base - headerHeight else base
    }

    fun updateBottomBarHeight(height: Dp, pageTag: String = "") {
        if (bottomBarHeight != height) {
            bottomBarHeight = height
            NewsChannelLog.debug("Page") { "[$pageTag] 品字形 BottomBar 高度：${height}" }
        }
    }

    fun updateHeaderHeight(height: Dp, pageTag: String = "") {
        if (headerHeight != height) {
            headerHeight = height
            NewsChannelLog.debug("Page") { "[$pageTag] 品字形 Header 高度：${height}" }
        }
    }

    fun updateTitleBarHeight(height: Dp, pageTag: String = "") {
        if (titleBarHeight != height) {
            titleBarHeight = height
            NewsChannelLog.debug("Page") { "[$pageTag] 品字形 TitleBar 高度：${height}" }
        }
    }

    fun updateHangingHeight(height: Dp, pageTag: String = "") {
        if (hangingHeight != height) {
            hangingHeight = height
            NewsChannelLog.debug("Page") { "[$pageTag] 品字形 hanging 高度：${height}" }
        }
    }

    fun updatePageSize(width: Dp, height: Dp, pageTag: String = "") {
        val desHeight = if (maxPageHeight > 0.dp && height > maxPageHeight) maxPageHeight
            else height
        if (pageViewWidth != width || pageViewHeight != desHeight) {
            pageViewWidth = width
            pageViewHeight = desHeight
            NewsChannelLog.debug("Page") { "[$pageTag] 品字形 页面宽高：${width}/${desHeight}, maxPageHeight: $maxPageHeight" }
        }
    }
}

@Composable
private fun rememberStructPageSize(pageTag: String = ""): StructPageSize {
    val pageWidth = pageViewWidth()
    val pageHeight = pageViewHeight()
    // 不以 pageWidth/pageHeight 为 remember key，避免尺寸变化时重建对象
    // StructPageSize 内部属性都是 mutableStateOf，响应式更新即可驱动 UI 刷新
    val pageSize = remember { StructPageSize() }
    pageSize.updatePageSize(pageWidth, pageHeight, pageTag)
    return pageSize
}

@Composable
internal fun WatchPageScrollFlow(
    scrollScaffold: StructPageScrollScaffold,
    displayItems: List<IKmmFeedsItem>?,
) {
    val pageScrollState = LocalStructPageViewModel.current?.scrollStateFlow
        ?: return
    LaunchedEffect(displayItems, scrollScaffold, pageScrollState) {
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

            // 有Item，滑动到对应Cell
            val currentItem = scrollState.currentItem
                ?: return@collectLatest
            val scrollIndex = displayItems?.indexOfFirst {
                it.baseDto.idStr == currentItem.baseDto.idStr
            } ?: INVALID_NUM
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
