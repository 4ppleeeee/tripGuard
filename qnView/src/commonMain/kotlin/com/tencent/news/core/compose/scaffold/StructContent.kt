@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.pager.HorizontalPager
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.news.core.annotation.PlatformRawApi
import com.tencent.news.core.annotation.PlatformRawApiReason
import com.tencent.news.core.compose.adaptive.AdaptiveContent
import com.tencent.news.core.compose.page.SubPageLayerView
import com.tencent.news.core.compose.scaffold.modifiers.DtCurrentView
import com.tencent.news.core.compose.scaffold.modifiers.dtPage
import com.tencent.news.core.compose.scaffold.modifiers.onSizeChangedDp
import com.tencent.news.core.compose.scaffold.modifiers.traversePage
import com.tencent.news.core.compose.view.list.IQnListState
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.IStructSubPage
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.service.ViewService
import com.tencent.news.core.view.setup.ViewServiceBridge
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
@OptIn(PlatformRawApi::class)
@PlatformRawApiReason("鸿蒙平台的beyondViewportPageCount即使置为0，滑动切换页签也会触发加载了其他的tab，历史页里有子频道有QnListView绑定了scrollScaffold的ListState，导致了竞态问题，针对鸿蒙平台特殊做了保护")
@Composable
internal fun StructMultiTabContent(
    modifier: Modifier = Modifier,
    scrollScaffold: StructPageScrollScaffold,
    pageWidget: StructPageWidget2,
    displayItems: List<IListItem>,
    pagerState: PagerState
) {
    val pagerWidget = pageWidget.pager ?: return
    val mainChannel = pagerWidget.mainChannel ?: return
    val channels = pagerWidget.channels.takeIfNotEmpty() ?: return
    var nativeRef by remember { mutableStateOf<DtCurrentView?>(null) }
    var canSlideByChild by remember { mutableStateOf(true) }

    DisposableEffect(pageWidget) {
        ViewServiceBridge.impl.registerParentPageSlidingCallback { canSlide ->
            canSlideByChild = canSlide
        }
        onDispose {
            ViewServiceBridge.impl.registerParentPageSlidingCallback { }
        }
    }

    HorizontalPager(
        modifier = modifier.fillMaxSize().bouncesEnable(false).onSizeChangedDp {
            NewsChannelLog.debug("Page") {
                "品字形 listHeight：${scrollScaffold.listHeight}, pager高度：${it.height}"
            }
        }.nativeRef { nativeRef = it },
        beyondViewportPageCount = pagerWidget.action.beyondViewportPageCount,
        state = pagerState,
        userScrollEnabled = !pageWidget.pageConfig.disableHorizontalPagerGesture && canSlideByChild,
        key = { index -> channels[index].data?.channel_info?.channelKey ?: index.toString() }
    ) { index ->
        val isSelected = index == pagerState.currentPage
        val channelWidget = channels[index]

        // 且 settle 后存在延迟重组，导致非可见page的StructSubPageView等重型组件被创建，
        // 其内部会竞争 scrollScaffold.selectedListState 或覆盖 CompositionLocal，
        // 引起当前选中tab布局错乱。
        // 安卓虽然也有相邻page被compose的行为，但重组时序紧凑，不会触发竞争问题。
        // 因此仅在鸿蒙平台 + beyondViewportPageCount=0 时，只对 currentPage/targetPage 渲染实际内容。
        val shouldRenderContent by remember(index) {
            derivedStateOf {
                if (!isHarmonyPlatform() || pagerWidget.action.beyondViewportPageCount > 0) {
                    true
                } else {
                    index == pagerState.currentPage || index == pagerState.targetPage
                }
            }
        }

        LaunchedEffect(channelWidget, isSelected) {
            channelWidget.status.isSelected.update { isSelected }
            if (isSelected) {
                traversePage(nativeRef)
            }
        }

        ChannelDtReport(channelWidget) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (shouldRenderContent) {
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
    Box {
        AdaptiveContent {
            QnListView(channelWidget, displayItems, selectedListState)
        }

        SubPageLayerView(pageWidget?.layers)
    }
}