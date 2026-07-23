@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.news.core.compose.page.DelayRenderEffect
import com.tencent.news.core.compose.page.SubPageLayerView
import com.tencent.news.core.compose.scaffold.modifiers.DtCurrentView
import com.tencent.news.core.compose.scaffold.modifiers.dtPage
import com.tencent.news.core.compose.scaffold.modifiers.iosLeadingEdgeSwipeBackPriority
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.scaffold.modifiers.onSizeChangedDp
import com.tencent.news.core.compose.scaffold.modifiers.traversePage
import com.tencent.news.core.compose.scaffold.registry.LocalStructTitleAreaHeight
import com.tencent.news.core.compose.scaffold.registry.nestedScroll
import com.tencent.news.core.compose.view.QnHorizontalPager
import com.tencent.news.core.compose.view.list.IQnListState
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.IStructSubPage
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.service.ViewService
import kotlinx.coroutines.flow.update


// 只支持单tab
@Composable
internal fun StructSingleTabContent(
    modifier: Modifier,
    scrollScaffold: StructPageScrollScaffold,
    pageWidget: StructPageWidget2,
    displayItems: List<IListItem>,
) {
    val mainChannel by pageWidget.pager?.mainChannelFlow?.collectAsState() ?: return

    mainChannel?.ApplyStatusBarStyleEffect()
    LaunchedEffect(mainChannel) {
        mainChannel?.status?.isSelected?.update { true }
    }

    ChannelDtReport(mainChannel) {
        Box(modifier) {
            StructMainList(mainChannel, scrollScaffold, displayItems)
        }
    }
}


// 支持多tab
@Composable
internal fun StructMultiTabContent(
    modifier: Modifier = Modifier,
    scrollScaffold: StructPageScrollScaffold,
    pageWidget: StructPageWidget2,
    displayItems: List<IListItem>,
    pagerState: PagerState,
    onChannelSwitchIntercept: ((Int) -> Boolean)?,
    disableChannelSwipe: Boolean = false,
) {
    val pagerWidget = pageWidget.pager ?: return
    val mainChannel = pagerWidget.mainChannel ?: return
    // 触发更新，不能删
    pagerWidget.asWidgetVM.structureVersionFlow.collectAsState().value
    val channels = pagerWidget.channels.takeIfNotEmpty() ?: return
    var nativeRef by remember { mutableStateOf<DtCurrentView?>(null) }

    WatchPageScrollFlow(pageWidget, pagerState)

    LaunchedEffect(channels.size, pagerState.currentPage) {
        if (pagerState.currentPage >= channels.size) {
            pagerState.scrollToPage(channels.lastIndex)
        }
    }
    val safeCurrentPage = pagerState.currentPage.coerceIn(0, channels.lastIndex)
    if (safeCurrentPage != pagerState.currentPage) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    val shouldHandleIosHorizontalPagerSwipeBack =
        pageWidget.pageConfig.enableIosLeadingEdgeSwipeBackForHorizontalPager && isIOSPlatform()

    QnHorizontalPager(
        modifier = modifier.fillMaxSize().bouncesEnable(false)
            .iosLeadingEdgeSwipeBackPriority(shouldHandleIosHorizontalPagerSwipeBack)
            .nestedScroll(isHorizontal = true)
            .onSizeChangedDp {
                NewsChannelLog.debug("Page") {
                    "品字形 listHeight：${scrollScaffold.listHeight}, pager高度：${it.height}"
                }
            }.nativeRef { nativeRef = it },
        beyondViewportPageCount = pagerWidget.action.beyondViewportPageCount,
        state = pagerState,
        key = { index ->
            channels.getOrNull(index)?.data?.channel_info?.channelKey?.takeIf { it.isNotBlank() } ?: index
        },
        userScrollEnabled = !pageWidget.pageConfig.disableHorizontalPagerGesture && !disableChannelSwipe,
        pagerName = { index -> channels.getOrNull(index)?.data?.channel_info?.channelName },
    ) { index ->
        val isSelected = index == pagerState.currentPage
        val channelWidget = channels.getOrNull(index) ?: return@QnHorizontalPager
        channelWidget.ApplyStatusBarStyleEffect()
        LaunchedEffect(channelWidget, isSelected) {
            channelWidget.status.isSelected.update { isSelected }
            if (isSelected) {
                traversePage(nativeRef)
            }
        }

        ChannelDtReport(channelWidget) {
            var topAdaptMargin = 0.dp
            if (channelWidget.status.adaptTitleAreaHeight) {
                topAdaptMargin = LocalStructTitleAreaHeight.current.value
            }
            // key 用于保证子tab的刷新
            val pageArgs by channelWidget.status.pageArgs.collectAsState()
            key(pageArgs) {
                Box(Modifier.margin(top = topAdaptMargin)) {
                    if (channelWidget == mainChannel && displayItems.isNotEmpty()) {
                        StructMainList(mainChannel, scrollScaffold, displayItems)
                    } else {
                        // 子tab支持按不同 ChannelWidget 注册实现
                        ViewService.channel.Build(channelWidget, scrollScaffold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelDtReport(widget: ChannelWidget?, content: @Composable () -> Unit) {
    val channelDtReport = widget?.dtReport
    if (channelDtReport != null) {
        Box(Modifier.dtPage(channelDtReport)) {
            content()
        }
    } else {
        content()
    }
}

@Composable
private fun StructMainList(
    mainChannel: ChannelWidget?,
    scrollScaffold: StructPageScrollScaffold,
    displayItems: List<IListItem>,
) {
    mainChannel ?: return

    val emptyWidget = mainChannel.empty

    if (displayItems.isEmpty()) {
        if (mainChannel is IStructSubPage) {
            // 子tab支持按不同 ChannelWidget 注册实现
            ViewService.channel.Build(mainChannel, scrollScaffold)
            return
        } else if (emptyWidget != null) {
            // 数据为空时显示 emptyView
            Box {
                ViewService.layer.Build(this, emptyWidget)
            }
            return
        }
    }

    // 直接展示内容数据：
    StructDisplayList(mainChannel, displayItems, scrollScaffold.selectedListState)
}

@Composable
private fun StructDisplayList(
    channelWidget: ChannelWidget,
    displayItems: List<IListItem>,
    selectedListState: MutableState<IQnListState?>,
) {
    val pageWidget = channelWidget.findStructPageWidget2()

    val isSelected by channelWidget.status.isSelected.collectAsState()
    val delayRenderType by channelWidget.status.delayRenderType.collectAsState()
    channelWidget.DelayRenderEffect(isSelected, delayRenderType)

    if (delayRenderType == null) {
        Box {
            QnListView(channelWidget, displayItems, selectedListState)

            SubPageLayerView(pageWidget?.layers)
        }
    }
}

@Composable
private fun WatchPageScrollFlow(pageWidget: StructPageWidget2, pagerState: PagerState) {
    val pagerVM = pageWidget.pager?.asWidgetVM ?: return
    val pageScrollState by pagerVM.pagerFlow.collectAsState()
    LaunchedEffect(pagerState, pageScrollState) {

        val scrollToPosition = pageScrollState?.scrollToPosition ?: -1

        if (scrollToPosition < 0) {
            return@LaunchedEffect
        }

        if (pageScrollState?.animated.isTrue()) {
            pagerState.animateScrollToPage(scrollToPosition)
        } else {
            pagerState.scrollToPage(scrollToPosition)
        }
    }
}
