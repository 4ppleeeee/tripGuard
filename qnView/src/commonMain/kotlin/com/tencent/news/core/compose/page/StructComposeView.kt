@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.alpha
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.modifiers.backgroundColor
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.compose.scaffold.BgImpl
import com.tencent.news.core.compose.scaffold.BottomBarImpl
import com.tencent.news.core.compose.scaffold.ChannelBarImpl
import com.tencent.news.core.compose.scaffold.HangingViewImpl
import com.tencent.news.core.compose.scaffold.HeaderImpl
import com.tencent.news.core.compose.scaffold.LayerImpl
import com.tencent.news.core.compose.scaffold.MainContentImpl
import com.tencent.news.core.compose.scaffold.StructMultiTabContent
import com.tencent.news.core.compose.scaffold.StructPageScaffold
import com.tencent.news.core.compose.scaffold.StructSingleTabContent
import com.tencent.news.core.compose.scaffold.TitleBarImpl
import com.tencent.news.core.compose.scaffold.WatchPageScrollFlow
import com.tencent.news.core.compose.scaffold.registry.LocalPageSkin
import com.tencent.news.core.compose.scaffold.registry.LocalStructPagePagerIndex
import com.tencent.news.core.compose.scaffold.skin.PageSkin
import com.tencent.news.core.compose.scaffold.skin.rememberStructPageSkinColor
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.scaffold.theme.QnSkin
import com.tencent.news.core.extension.safeSize
import com.tencent.news.core.list.controller.FeedsProcessResult
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.page.model.PagerWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.service.ViewService

// 品字形页面view
@OptIn(KmmInternalApi::class)
@Composable
internal fun StructComposeView(
    uiCustomize: StructPageUICustomize?,
    pageWidget: StructPageWidget2,
    feedsResult: FeedsProcessResult,
) {
    val pagerState = getPagerState(pageWidget.pager)
    val pagerIndexState = remember { derivedStateOf { pagerState.currentPage } }

    val skinColorState = rememberStructPageSkinColor(pageWidget.getValidPageTheme())

    CompositionLocalProvider(
        LocalStructPagePagerIndex provides pagerIndexState,
        LocalPageSkin provides skinColorState
    ) {
        // 皮肤Modifier 目前只有专题有
        val currentSkinColor = skinColorState.value
        val pageModifier = uiCustomize?.pageModifier ?: createDefaultSkinModifier(currentSkinColor)
        StructPageScaffold(
            modifier = pageModifier,
            pageWidget = pageWidget,

            titleBar = pageWidget.structTitleBar(),
            bottomBar = pageWidget.structBottomBar(),
            header = pageWidget.structHeader(),
            hangingView = pageWidget.structHangingView(),
            channelBar = pageWidget.structChannelBar(pagerState),
            mainContent = pageWidget.structContent(feedsResult, uiCustomize, pagerState),
            layerView = pageWidget.structLayer(),
            bgView = pageWidget.structBgPage(),
        )
    }
}

private fun StructPageWidget2.structTitleBar(): TitleBarImpl? =
    titleBar?.let { widget ->
        {
            debugLog { "TitleBar 发生重组，请确认是否应该优化！" }
            ViewService.titleBar.Build(widget)
        }
    }

private fun StructPageWidget2.structBottomBar(): BottomBarImpl? =
    bottomBar?.let { widget ->
        {
            debugLog { "BottomBar 发生重组，请确认是否应该优化！" }
            ViewService.bottomBar.Build(widget)
        }
    }

private fun StructPageWidget2.structHeader(): HeaderImpl? =
    {
        debugLog { "Header 发生重组，请确认是否应该优化！" }
        val header by headerFlow.collectAsState()
        ViewService.header.Build(header)
    }

private fun StructPageWidget2.structHangingView(): HangingViewImpl? =
    hanging?.let { widget ->
        {
            debugLog { "Hanging 发生重组，请确认是否应该优化！" }
            ViewService.hanging.Build(widget)
        }
    }

private fun StructPageWidget2.structChannelBar(pagerState: PagerState): ChannelBarImpl? =
    pager?.channelBar?.takeIf { it.canShowChannelBar() }?.let { widget ->
        {
            debugLog { "ChannelBar 发生重组，请确认是否应该优化！" }
            ViewService.channelBar.Build(pagerState, widget)
        }
    }

private fun StructPageWidget2.structContent(
    feedsResult: FeedsProcessResult,
    uiCustomize: StructPageUICustomize?,
    pagerState: PagerState,
): MainContentImpl =
    { scrollScaffold ->
        val pageWidget = this
        val displayItems = feedsResult.allData

        pageWidget.debugLog { "【警告】Content 发生重组，请确认是否应该优化！" }

        // 页面滚动事件监听：
        WatchPageScrollFlow(scrollScaffold, displayItems)

        PagerLoadingView(pageWidget.pager) {
            val containerModifier = if (QnSkin != null) {
                Modifier.fillMaxWidth().padding(
                    horizontal = 8.dp
                )
            } else {
                Modifier
            }
            val contentModifier = uiCustomize?.contentModifier ?: Modifier
            Box(modifier = containerModifier) {
                if (pageWidget.hasMultiChannels()) {
                    StructMultiTabContent(
                        contentModifier, scrollScaffold, pageWidget, displayItems, pagerState
                    )
                } else {
                    StructSingleTabContent(
                        contentModifier, scrollScaffold, pageWidget, displayItems
                    )
                }
            }
        }
    }

private fun StructPageWidget2.structLayer(): LayerImpl? =
    layers?.getMainPageWidgets()?.let { layerWidgets ->
        {
            debugLog { "Layer 发生重组，请确认是否应该优化！" }
            layerWidgets.forEach { widget ->
                ViewService.layer.Build(this, widget)
            }
        }
    }

private fun StructPageWidget2.structBgPage(): BgImpl? =
    bg?.let { bgWidget ->
        {
            debugLog { "Bg 发生重组，请确认是否应该优化！" }
            ViewService.layer.Build(this, bgWidget)
        }
    }

private inline fun StructPageWidget2.debugLog(msg: () -> String) {
    ComposeViewLog.debug("Struct") {
        "${this::class.simpleName}_${msg()}"
    }
}

@Composable
private fun PagerLoadingView(
    pagerWidget: PagerWidget?,
    content: @Composable () -> Unit
) {
    val loadingWidget = pagerWidget?.loading
    if (loadingWidget == null) {
        content()
        return
    }

    val loadingState = pagerWidget.asWidgetVM.loadingFlow.collectAsState()
    val isLoading by remember { loadingState }

    if (isLoading) {
        Box {
            ViewService.layer.Build(this, loadingWidget)
        }
    }

    // 主列表不放到if条件里面，避免条件变化重新构造新的view（listState可能错乱，导致页面白屏）
    Box(Modifier.alpha(if (isLoading) 0f else 1f)) {
        content()
    }
}

@Composable
private fun getPagerState(pagerWidget: PagerWidget?): PagerState {
    return rememberPagerState(
        initialPage = pagerWidget?.action?.initIndex ?: 0,
        pageCount = { pagerWidget?.channels.safeSize() }
    )
}

@Composable
private fun createDefaultSkinModifier(currentSkinColor: PageSkin?): Modifier {
    return if (currentSkinColor?.skinColor != null) {
        Modifier.fillMaxWidth().backgroundColor(currentSkinColor.skinColor)
    } else {
        Modifier.backgroundColor(QnColor.bgPage)
    }
}