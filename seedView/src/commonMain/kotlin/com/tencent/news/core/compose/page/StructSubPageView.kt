package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import com.tencent.kuikly.compose.kuikly.extension.bouncesEnable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.compose.scaffold.QnListView
import com.tencent.news.core.compose.scaffold.StructPage
import com.tencent.news.core.compose.scaffold.StructPageScrollScaffold
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.getAllFeedsList
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.WatchPageScrollFlow
import com.tencent.news.core.compose.scaffold.ApplyStatusBarStyleEffect
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.compose.scaffold.registry.LocalStructChannelOffset
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.vm.StructPageViewModel
import com.tencent.news.core.compose.view.QnHorizontalPager
import com.tencent.news.core.extension.safeSize
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.IStructSubPage
import com.tencent.news.core.page.model.StructPageData
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.service.FrameworkService
import com.tencent.news.core.service.ViewService
import com.tencent.news.core.util.lifecycle.PageLifecycleFlow
import kotlinx.coroutines.flow.update

/**
 * 品字形架构：通用子页面渲染组件（支持二级 Pager）
 *
 * ## 使用场景
 * - 需要二级 tab 的子页面（如"我的历史"、"关注"等）
 * - 普通列表子页面（可替代 StructChannelList）
 *
 * @param scrollScaffold 滚动脚手架，提供滚动状态和列表状态
 * @param channelWidget 频道配置，包含数据源和渲染配置
 */
@Composable
fun StructSubPageView(
    scrollScaffold: StructPageScrollScaffold,
    channelWidget: ChannelWidget,
) {
    val pageScope = rememberCoroutineScope()
    val pageFlow = LocalComposePageLifecycleFlow.current ?: PageLifecycleFlow().lifecycleFlow

    // 创建子页面的 ViewModel
    val subPageViewModel = remember {
        val feedsCtrl = FrameworkService.createOrGetFlexController(channelWidget)

        val subPageVMCreator = (channelWidget as? IStructSubPage)?.subPageVM
        val viewModel = if (subPageVMCreator != null) {
            subPageVMCreator(feedsCtrl, pageFlow, pageScope)
        } else {
            StructPageViewModel(
                controller = feedsCtrl,
                pageFlow = pageFlow,
                pageScope = pageScope
            )
        }

        channelWidget.subTabPageViewModel = viewModel

        viewModel
    }

    // 初始化刷新逻辑
    LaunchedEffect(Unit) {
        val refreshAction = if (subPageViewModel.getAllFeedsList().isNotEmpty()) {
            ListRefreshAction.AUTO_CACHE
        } else {
            ListRefreshAction.RESET_CHANNEL
        }
        subPageViewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET, refreshAction))
    }

    // 网络状态监听
    SubPageNetworkEffect(subPageViewModel)

    DisposableEffect(subPageViewModel) {
        onDispose {
            subPageViewModel.onPageDisposed()
        }
    }

    val pageState = subPageViewModel.loadingStateFlow.collectAsState()
    val uiState by pageState

    val channelOffset = remember {
        derivedStateOf {
            scrollScaffold.pageHeight - scrollScaffold.listHeight
        }
    }

    // 获取子页面的 StructPageWidget2（用于获取静态 pager 配置）
    val subPageWidget = remember {
        (channelWidget as? IStructSubPage)?.subPageWidget?.invoke(channelWidget)
    }

    // 子 Tab SubPageWidget 上挂载的 PageReportAction 收口：
    // 让 SubPageWidget.pageConfig.reportAction 在子 Tab 进入/退出（结合 QnHorizontalPager 注入的
    // LocalComposePageLifecycleFlow）时触发 onPageResume/onPagePause/onPageExit，
    // 修复 SubPageWidget 上 reportAction 长期是死代码、page_visit 漏报的问题。
    if (subPageWidget != null) {
        CollectPageReportAction(subPageWidget)
    }

    CompositionLocalProvider(
        LocalStructPageViewModel provides subPageViewModel,
        LocalStructChannelOffset provides channelOffset
    ) {
        StructPage<StructPageData>(
            uiState = uiState,
            loadStateWidget = channelWidget,
            onRetryClick = {
                subPageViewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET))
            }
        ) { pageData ->
            val displayItems = pageData.feedsResult.allData

            // 优先从 subPageWidget 中获取 pager 数据（静态配置）
            // 如果没有，则从网络请求返回的 pageWidget 中获取（动态配置）
            val pagerWidget = subPageWidget?.pager ?: pageData.pageWidget.pager

            if (pagerWidget != null) {
                // 【模式1】渲染二级 Pager 结构（ChannelBar + HorizontalPager）
                RenderPagerContent(
                    pagerWidget = pagerWidget,
                    scrollScaffold = scrollScaffold,
                    displayItems = displayItems,
                    subPageViewModel = subPageViewModel,
                    subPageWidget = subPageWidget,
                )
            } else {
                // 【模式2】渲染普通列表（等同于 StructChannelList）
                RenderListContent(
                    channelWidget = channelWidget,
                    scrollScaffold = scrollScaffold,
                    displayItems = displayItems,
                    subPageViewModel = subPageViewModel
                )
            }
        }
    }
}

/**
 * 渲染二级 Pager 内容（ChannelBar + HorizontalPager）
 */
@Composable
private fun RenderPagerContent(
    pagerWidget: com.tencent.news.core.page.model.PagerWidget,
    scrollScaffold: StructPageScrollScaffold,
    displayItems: List<IKmmFeedsItem>,
    subPageViewModel: IStructPageViewModel,
    subPageWidget: StructPageWidget2? = null,
) {
    // 优先使用 channelBarVM 中保存的 selectedIndex 恢复二级tab位置
    // 当一级tab切走再切回时，pagerState 会被重新创建，但 channelBarVM 的 selectedIndex 仍保留之前的值
    val restoredIndex = pagerWidget.channelBar?.vm?.selectedIndex?.value
        ?: pagerWidget.action.initIndex
    val pageCount = pagerWidget.channels.safeSize()
    val safeInitialPage = restoredIndex.coerceInPageCount(pageCount)
    val pagerState = rememberPagerState(
        initialPage = safeInitialPage,
        pageCount = { pagerWidget.channels.safeSize() }
    )
    val channels = pagerWidget.channels.takeIfNotEmpty() ?: return
    val safeCurrentPage = pagerState.currentPage.coerceIn(0, channels.lastIndex)
    if (safeCurrentPage != pagerState.currentPage) {
        LaunchedEffect(pagerState, channels.size, safeCurrentPage) {
            pagerState.scrollToPage(safeCurrentPage)
        }
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // 注意：因为嵌套了2层pager，必须替换 selectedListState 对象，否则会有重组死循环；
    // 如果2个子tab都有pager结构，他们都持有外层大页面的 scrollScaffold，同时更新 selectedListState 导致
    val subPageScaffold = remember(scrollScaffold) {
        scrollScaffold.copy(selectedListState = mutableStateOf(null))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 渲染二级 ChannelBar
        val channelBarWidget = pagerWidget.channelBar
        if (channelBarWidget != null && channelBarWidget.canShowChannelBar()) {
            ViewService.channelBar.Build(pagerState, channelBarWidget, null)
        }

        // 渲染二级 Pager 内容
        Box(modifier = Modifier.weight(1f)) {
            QnHorizontalPager(
                modifier = Modifier.fillMaxSize().bouncesEnable(false),
                beyondViewportPageCount = pagerWidget.action.beyondViewportPageCount,
                state = pagerState,
                userScrollEnabled = !(subPageWidget?.pageConfig?.disableHorizontalPagerGesture
                    ?: false),
                pagerName = { index -> channels[index].data?.channel_info?.channelName },
                key = { index -> channels[index].data?.channel_info?.channelKey.orEmpty() }
            ) { index ->
                val channel = channels[index]
                channel.ApplyStatusBarStyleEffect()
                val isSelected = index == pagerState.currentPage
                LaunchedEffect(channel, isSelected) {
                    channel.status.isSelected.update { isSelected }
                }

                // 为每个二级tab创建独立的ViewModel并触发数据加载
                if (channel is IStructSubPage) {
                    val pageArgs by channel.status.pageArgs.collectAsState()
                    key(pageArgs) {
                        StructChannelList(subPageScaffold, channel)
                    }
                } else {
                    // 直接渲染列表，不调用 ViewService.channel.Build，避免嵌套
                    Box {
                        val emptyWidget = channel.empty
                        if (emptyWidget != null && displayItems.isEmpty()) {
                            ViewService.layer.Build(this, emptyWidget)
                        } else {
                            // 页面滚动事件监听
                            WatchPageScrollFlow(subPageScaffold, displayItems)
                            QnListView(channel, displayItems, subPageScaffold.selectedListState)
                            subPageViewModel.onAfterShowMainContent()
                        }

                        SubPageLayerView(subPageViewModel.pageRootWidget.layers)
                    }
                }
            }
        }
    }
}

private fun Int.coerceInPageCount(pageCount: Int): Int {
    return if (pageCount > 0) {
        coerceIn(0, pageCount - 1)
    } else {
        0
    }
}

/**
 * 渲染普通列表内容（等同于 StructChannelList）
 */
@Composable
private fun RenderListContent(
    channelWidget: ChannelWidget,
    scrollScaffold: StructPageScrollScaffold,
    displayItems: List<IKmmFeedsItem>,
    subPageViewModel: IStructPageViewModel
) {
    Box {
        val emptyWidget = channelWidget.empty
        if (emptyWidget != null && displayItems.isEmpty()) {
            ViewService.layer.Build(this, emptyWidget)
        } else {
            // 页面滚动事件监听
            WatchPageScrollFlow(scrollScaffold, displayItems)
            QnListView(channelWidget, displayItems, scrollScaffold.selectedListState)
            subPageViewModel.onAfterShowMainContent()
        }

        SubPageLayerView(subPageViewModel.pageRootWidget.layers)
    }
}
