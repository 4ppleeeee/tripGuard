package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.pager.HorizontalPager
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose_dsl.kuikly.extension.bouncesEnable
import com.tencent.news.core.compose.adaptive.AdaptiveContent
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.compose.scaffold.QnListView
import com.tencent.news.core.compose.scaffold.StructPage
import com.tencent.news.core.compose.scaffold.StructPageScrollScaffold
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.getAllFeedsList
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.WatchPageScrollFlow
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.compose.scaffold.registry.LocalStructChannelOffset
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.vm.StructPageViewModel
import com.tencent.news.core.extension.safeSize
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.IStructSubPage
import com.tencent.news.core.page.model.StructPageData
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
        val subPageVM = if (subPageVMCreator != null) {
            subPageVMCreator(feedsCtrl, pageFlow, pageScope)
        } else {
            StructPageViewModel(
                controller = feedsCtrl,
                pageFlow = pageFlow,
                pageScope = pageScope
            )
        }

        channelWidget.subTabPageViewModel = subPageVM
        subPageVM
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

    // 生命周期管理
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
        (channelWidget as? IStructSubPage)?.subPageWidget?.invoke()
    }

    CompositionLocalProvider(
        LocalStructPageViewModel provides subPageViewModel,
        LocalStructChannelOffset provides channelOffset
    ) {
        val background = channelWidget.backgroundColor?.let { Color(it) }

        StructPage<StructPageData>(
            uiState = uiState,
            background = background,
            onRetryClick = {
                subPageViewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET))
            }
        ) { pageData ->
            val displayItems = pageData.feedsResult.allData

            // 优先从 subPageWidget 中获取 pager 数据（静态配置）
            // 如果没有，则从网络请求返回的 pageWidget 中获取（动态配置）
            val pagerWidget = subPageWidget?.pager ?: pageData.pageWidget.pager
            val isParentSelected by channelWidget.status.isSelected.collectAsState()

            if (pagerWidget != null) {
                // 【模式1】渲染二级 Pager 结构（ChannelBar + HorizontalPager）
                RenderPagerContent(
                    pagerWidget = pagerWidget,
                    scrollScaffold = scrollScaffold,
                    displayItems = displayItems,
                    subPageViewModel = subPageViewModel,
                    isParentSelected = isParentSelected
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
    isParentSelected: Boolean
) {
    // 优先使用 channelBarVM 中保存的 selectedIndex 恢复二级tab位置
    // 当一级tab切走再切回时，pagerState 会被重新创建，但 channelBarVM 的 selectedIndex 仍保留之前的值
    val restoredIndex = pagerWidget.channelBar?.vm?.selectedIndex?.value
        ?: pagerWidget.action.initIndex
    val pagerState = rememberPagerState(
        initialPage = restoredIndex,
        pageCount = { pagerWidget.channels.safeSize() }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // 渲染二级 ChannelBar
        val channelBarWidget = pagerWidget.channelBar
        if (channelBarWidget != null && channelBarWidget.canShowChannelBar()) {
            AdaptiveContent {
                ViewService.channelBar.Build(pagerState, channelBarWidget)
            }
        }

        // 渲染二级 Pager 内容
        Box(modifier = Modifier.weight(1f)) {
            val channels = pagerWidget.channels.takeIfNotEmpty() ?: return@Column

            HorizontalPager(
                modifier = Modifier.fillMaxSize().bouncesEnable(false),
                beyondViewportPageCount = pagerWidget.action.beyondViewportPageCount,
                state = pagerState,
                key = { index -> channels[index].data?.channel_info?.channelKey.orEmpty() }
            ) { index ->
                val channel = channels[index]
                val isSelected = isParentSelected && index == pagerState.currentPage

                LaunchedEffect(channel, isSelected) {
                    channel.status.isSelected.update { isSelected }
                }

                // 为每个二级tab创建独立的ViewModel并触发数据加载
                if (channel is IStructSubPage) {
                    StructChannelList(scrollScaffold, channel)
                } else {
                    // 直接渲染列表，不调用 ViewService.channel.Build，避免嵌套
                    Box {
                        val emptyWidget = channel.empty
                        if (emptyWidget != null && displayItems.isEmpty()) {
                            ViewService.layer.Build(this, emptyWidget)
                        } else {
                            StructContentBg(channel)

                            // 页面滚动事件监听
                            WatchPageScrollFlow(scrollScaffold, displayItems)
                            AdaptiveContent {
                                QnListView(channel, displayItems, scrollScaffold.selectedListState)
                            }
                            subPageViewModel.onAfterShowMainContent()
                        }

                        SubPageLayerView(subPageViewModel.pageRootWidget.layers)
                    }
                }
            }
        }
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
            StructContentBg(channelWidget)

            // 页面滚动事件监听
            WatchPageScrollFlow(scrollScaffold, displayItems)
            AdaptiveContent {
                QnListView(channelWidget, displayItems, scrollScaffold.selectedListState)
            }
            subPageViewModel.onAfterShowMainContent()
        }

        SubPageLayerView(subPageViewModel.pageRootWidget.layers)
    }
}