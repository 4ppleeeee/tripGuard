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
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.BuildStateViewOrElse
import com.tencent.news.core.compose.scaffold.QnListView
import com.tencent.news.core.compose.scaffold.StructPage
import com.tencent.news.core.compose.scaffold.StructPageLoadingView
import com.tencent.news.core.compose.scaffold.StructPageScrollScaffold
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.getAllFeedsList
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.pageRootWidget
import com.tencent.news.core.compose.scaffold.WatchPageScrollFlow
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.compose.scaffold.registry.LocalErrorImagePainterProvider
import com.tencent.news.core.compose.scaffold.registry.LocalStructChannelOffset
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.vm.StructPageViewModel
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.DelayRenderType
import com.tencent.news.core.page.model.IStructSubPage
import com.tencent.news.core.page.model.StructPageData
import com.tencent.news.core.service.FrameworkService
import com.tencent.news.core.service.ViewService
import com.tencent.news.core.util.lifecycle.PageLifecycleFlow

// 品字形架构：通用子tab组件
@Composable
fun StructChannelList(
    scrollScaffold: StructPageScrollScaffold,
    channelWidget: ChannelWidget,
) {
    val pageScope = rememberCoroutineScope()
    // 直接使用 QnHorizontalPager 注入的生命周期流（已包含 isSelected + parentResumed 双维度）
    val pageFlow = LocalComposePageLifecycleFlow.current ?: PageLifecycleFlow().lifecycleFlow

    // 注意：这个是子tab自己用的 pageViewModel，只负责这个tab自己的数据刷新
    val subPageViewModel = remember(channelWidget) {
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

        // 将ViewModel存储到ChannelWidget中，方便外部访问（例如：广场item插入后触发刷新）
        channelWidget.subTabPageViewModel = viewModel

        viewModel
    }

    // 子 Tab SubPageWidget 上挂载的 PageReportAction 收口：
    // 让 SubPageWidget.pageConfig.reportAction 在子 Tab 进入/退出（结合 QnHorizontalPager 注入的
    // LocalComposePageLifecycleFlow）时触发 onPageResume/onPagePause/onPageExit，
    // 修复 SubPageWidget 上 reportAction 长期是死代码、page_visit 漏报的问题。
    val subPageWidgetForReport = remember(channelWidget) {
        (channelWidget as? IStructSubPage)?.subPageWidget?.invoke(channelWidget)
    }
    if (subPageWidgetForReport != null) {
        CollectPageReportAction(subPageWidgetForReport)
    }

    LaunchedEffect(subPageViewModel) {

        // 有缓存先返回
        val hasCache = subPageViewModel.getAllFeedsList().isNotEmpty()
        if (hasCache) {
            // 内存中已有缓存数据，直接刷新，不用再拉取
            val request =
                FeedsRefreshRequest(ListRefreshForward.RESET, ListRefreshAction.AUTO_CACHE)
            subPageViewModel.refresh(request)
        }

        val hasNetworkData = channelWidget.status.hasNetworkData
        channelWidget.status.hasNetworkData = true
        val action = when {
            hasCache && !hasNetworkData -> ListRefreshAction.CACHE_AFTER_RESET
            !hasCache -> ListRefreshAction.RESET_CHANNEL
            else -> null
        }

        if (action != null) {
            subPageViewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET, action))
        }
    }

    // 网络状态监听
    SubPageNetworkEffect(subPageViewModel)

    DisposableEffect(subPageViewModel) {
        onDispose {
            subPageViewModel.onPageDisposed()
        }
    }

    val pageState = subPageViewModel.loadingStateFlow.collectAsState()
    val uiState by remember { pageState }

    // 注意：子tab的pageViewModel要重新provide一下，否则footer里取到的是整个页面pageViewModel，刷新有异常

    val channelOffset = remember(channelWidget) {
        derivedStateOf {
            scrollScaffold.pageHeight - scrollScaffold.listHeight
        }
    }

    CompositionLocalProvider(
        LocalStructPageViewModel provides subPageViewModel,
        LocalStructChannelOffset provides channelOffset
    ) {
        // 获取子页面背景色
        val background = channelWidget.backgroundColor?.let { Color(it) }

        // 获取渐变背景配置
        val gradientBrush = channelWidget.backgroundGradientStops?.let { stops ->
            Brush.linearGradient(
                colorStops = stops.map { (position, color) ->
                    position to Color(color)
                }.toTypedArray(),
                start = Offset(0f, 0f),
                end = Offset(0f, Float.POSITIVE_INFINITY)
            )
        }

        val errorImagePainter = LocalErrorImagePainterProvider.current?.invoke()

        val isSelected by channelWidget.status.isSelected.collectAsState()
        val delayRenderType by channelWidget.status.delayRenderType.collectAsState()
        channelWidget.DelayRenderEffect(isSelected, delayRenderType)

        if (delayRenderType == null) {
            StructPage<StructPageData>(
                uiState = uiState,
                loadStateWidget = channelWidget,
                background = background,
                onRetryClick = {
                    subPageViewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET))
                },
                forceDarkTheme = channelWidget.forceDarkTheme,
                errorImagePainter = errorImagePainter,
            ) {
                val displayItems = it.feedsResult.allData
                val modifier = if (gradientBrush != null) {
                    Modifier.fillMaxSize().background(gradientBrush)
                } else {
                    Modifier
                }

                Box(
                    modifier = modifier
                ) {
                    val emptyWidget = channelWidget.empty
                    if (emptyWidget != null && displayItems.isEmpty()) {
                        ViewService.layer.Build(this, emptyWidget)
                    } else {
                        // 页面滚动事件监听（子tab的pageVM也支持滑动）：
                        WatchPageScrollFlow(scrollScaffold, displayItems)

                        QnListView(
                            channelWidget,
                            displayItems,
                            scrollScaffold.selectedListState
                        )

                        subPageViewModel.onAfterShowMainContent()
                    }

                    SubPageLayerView(subPageViewModel.pageRootWidget.layers)
                }
            }
        } else if (delayRenderType == DelayRenderType.SiblingTab) {
            channelWidget.BuildStateViewOrElse({ loading }) {
                StructPageLoadingView()
            }
        }

    }
}
