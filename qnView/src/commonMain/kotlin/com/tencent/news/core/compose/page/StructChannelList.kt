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
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.adaptive.AdaptiveContent
import com.tencent.news.core.compose.scaffold.QnListView
import com.tencent.news.core.compose.scaffold.StructPage
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
    val pageFlow = LocalComposePageLifecycleFlow.current ?: PageLifecycleFlow().lifecycleFlow

    // 注意：这个是子tab自己用的 pageViewModel，只负责这个tab自己的数据刷新
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

        // 将ViewModel存储到ChannelWidget中，方便外部访问（例如：广场item插入后触发刷新）
        channelWidget.subTabPageViewModel = viewModel

        viewModel
    }

    LaunchedEffect(Unit) {
        val refreshAction = if (subPageViewModel.getAllFeedsList().isNotEmpty()) {
            // 内存中已有缓存数据，直接刷新，不用再拉取
            ListRefreshAction.AUTO_CACHE
        } else {
            ListRefreshAction.RESET_CHANNEL
        }
        subPageViewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET, refreshAction))
    }

    DisposableEffect(subPageViewModel) {
        onDispose {
            subPageViewModel.onPageDisposed()
        }
    }

    val pageState = subPageViewModel.loadingStateFlow.collectAsState()
    val uiState by remember { pageState }

    // 注意：子tab的pageViewModel要重新provide一下，否则footer里取到的是整个页面pageViewModel，刷新有异常

    val channelOffset = remember {
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
        
        StructPage<StructPageData>(
            uiState = uiState,
            background = background,
            onRetryClick = {
                subPageViewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET))
            },
            forceDarkTheme = channelWidget.forceDarkTheme,
            errorImagePainter = errorImagePainter,
        ) {
            val displayItems = it.feedsResult.allData

            Box(
                modifier = if (gradientBrush != null) {
                    Modifier.fillMaxSize().background(gradientBrush)
                } else {
                    Modifier
                }
            ) {
                val emptyWidget = channelWidget.empty
                if (emptyWidget != null && displayItems.isEmpty()) {
                    ViewService.layer.Build(this, emptyWidget)
                } else {
                    StructContentBg(channelWidget)

                    // 页面滚动事件监听（子tab的pageVM也支持滑动）：
                    WatchPageScrollFlow(scrollScaffold, displayItems)

                    AdaptiveContent {
                        QnListView(channelWidget, displayItems, scrollScaffold.selectedListState)
                    }

                    subPageViewModel.onAfterShowMainContent()
                }

                SubPageLayerView(subPageViewModel.pageRootWidget.layers)
            }
        }
    }

}

@Composable
internal fun BoxScope.StructContentBg(channelWidget: ChannelWidget) {
    channelWidget.contentBg?.let { contentBg ->
        ViewService.layer.Build(this, contentBg)
    }
}