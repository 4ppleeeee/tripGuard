@file:Suppress("FunctionNaming")

package com.tencent.news.core.view.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.compose.scaffold.DefaultChannelBar
import com.tencent.news.core.compose.scaffold.StructPageScrollScaffold
import com.tencent.news.core.compose.scaffold.card.FeedsItemCtx
import com.tencent.news.core.compose.scaffold.card.IFeedsItemCardService
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatus
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.scaffold.widgetbtns.StructBottomBar
import com.tencent.news.core.compose.scaffold.widgetbtns.StructTitleBar
import com.tencent.news.core.compose.scaffold.widgetbtns.currentTitleBarTheme
import com.tencent.news.core.compose.scaffold.widgetbtns.getTitleBarWidgetColor
import com.tencent.news.core.compose.share.PostPreviewData
import com.tencent.news.core.compose.share.ShareResult
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.SpacerHeight
import com.tencent.news.core.list.model.ChannelShowType
import com.tencent.news.core.page.model.BottomBarWidget
import com.tencent.news.core.page.model.ChannelBarWidget
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.CommonHeaderWidget
import com.tencent.news.core.page.model.CommonTitleBarWidget
import com.tencent.news.core.page.model.HeaderWidget
import com.tencent.news.core.page.model.SchemeBtnWidget
import com.tencent.news.core.page.model.StructWidget
import com.tencent.news.core.page.model.StructWidgetType
import com.tencent.news.core.page.model.TitleBtnWidget
import com.tencent.news.core.compose.scaffold.widgetbtns.SchemeBtn
import com.tencent.news.core.service.IStructBottomBarRegistry
import com.tencent.news.core.service.IStructBtnRegistry
import com.tencent.news.core.service.IStructChannelBarRegistry
import com.tencent.news.core.service.IStructChannelRegistry
import com.tencent.news.core.service.IStructHangingRegistry
import com.tencent.news.core.service.IStructHeaderRegistry
import com.tencent.news.core.service.IStructLayerRegistry
import com.tencent.news.core.service.IStructTitleBarRegistry
import com.tencent.news.core.service.IViewServiceRegistry
import com.tencent.news.core.setup.registerImpl
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.share.model.IShareContent
import com.tencent.news.core.vm.IFeedsVMItemStub

private const val SEED_PIN_CONTENT_PREFIX = "seed_pin_content_"

@OptIn(KmmInternalApi::class)
fun setupDefaultViewService() {
    IViewServiceRegistry.registerImpl { DefaultViewServiceRegistry }
    ViewServiceBridge.register(DefaultViewServiceBridge)
}

private object DefaultViewServiceRegistry : IViewServiceRegistry {
    override val itemCard: IFeedsItemCardService = DefaultFeedsItemCardService
    override val layer: IStructLayerRegistry = DefaultLayerRegistry
    override val titleBar: IStructTitleBarRegistry = DefaultTitleBarRegistry
    override val header: IStructHeaderRegistry = DefaultHeaderRegistry
    override val bottomBar: IStructBottomBarRegistry = DefaultBottomBarRegistry
    override val channelBar: IStructChannelBarRegistry = DefaultChannelBarRegistry
    override val hanging: IStructHangingRegistry = DefaultHangingRegistry
    override val btn: IStructBtnRegistry = DefaultBtnRegistry
    override val channel: IStructChannelRegistry = DefaultChannelRegistry
    override val share = DefaultShareComponentRegistry
}

private object DefaultTitleBarRegistry : IStructTitleBarRegistry {
    @Composable
    override fun Build(titleBarWidget: CommonTitleBarWidget) {
        StructTitleBar(titleBarWidget)
    }
}

private object DefaultHeaderRegistry : IStructHeaderRegistry {
    @Composable
    override fun Build(headerWidget: HeaderWidget?) {
        headerWidget ?: return
        if (headerWidget is CommonHeaderWidget) {
            SeedHeader(headerWidget)
            return
        }
        Column(Modifier.fillMaxWidth()) {
            headerWidget.headerList.orEmpty().forEach {
                Box(Modifier.fillMaxWidth()) {
                    DefaultLayerRegistry.Build(this, it)
                }
            }
        }
    }
}

private object DefaultBottomBarRegistry : IStructBottomBarRegistry {
    @Composable
    override fun Build(bottomWidget: BottomBarWidget?) {
        StructBottomBar(bottomWidget)
    }
}

private object DefaultChannelBarRegistry : IStructChannelBarRegistry {
    @Composable
    override fun Build(
        pagerState: PagerState,
        channelBarWidget: ChannelBarWidget,
        onChannelSwitchIntercept: ((Int) -> Boolean)?,
    ) {
        DefaultChannelBar(channelBarWidget.widgetChannelBarVM(), pagerState)
    }
}

private object DefaultHangingRegistry : IStructHangingRegistry {
    @Composable
    override fun Build(hangingWidget: StructWidget) {
        DefaultLayerRegistry.Build(NoopBoxScope, hangingWidget)
    }
}

private object DefaultBtnRegistry : IStructBtnRegistry {
    @Composable
    override fun Build(btnWidget: StructWidget) {
        when (btnWidget) {
            is SchemeBtnWidget -> {
                SchemeBtn(btnWidget)
            }
            is TitleBtnWidget -> {
                val isHeaderCollapsed by LocalHeaderCollapseStatus.current
                QnText(
                    text = btnWidget.data?.title.orEmpty(),
                    color = getTitleBarWidgetColor(
                        isHeaderCollapsed = isHeaderCollapsed,
                        defaultColor = currentTitleBarTheme.titleTextColor,
                    ),
                    fontSize = (btnWidget.data?.fontSize?.takeIf { it > 0f } ?: 16f).sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            else -> QnText(
                text = btnWidget.getWidgetType(),
                color = QnColor.t2,
                fontSize = 12.sp,
            )
        }
    }
}

private object DefaultLayerRegistry : IStructLayerRegistry {
    @Composable
    override fun Build(boxScope: BoxScope, widget: StructWidget) {
        when {
            widget.widget_id.startsWith(SEED_PIN_CONTENT_PREFIX) -> SeedPinContent(widget.widget_id)
            widget is CommonHeaderWidget -> SeedHeader(widget)
            widget.getWidgetType() == StructWidgetType.SIMPLE_WIDGET -> SeedPinContent(widget.widget_id)
            else -> DefaultWidgetPlaceholder(widget)
        }
    }
}

private object DefaultChannelRegistry : IStructChannelRegistry {
    @Composable
    override fun Build(channelWidget: ChannelWidget, scrollScaffold: StructPageScrollScaffold) {
        val emptyWidget = channelWidget.empty
        if (emptyWidget != null) {
            DefaultLayerRegistry.Build(NoopBoxScope, emptyWidget)
            return
        }
        DefaultWidgetPlaceholder(channelWidget)
    }
}

private object DefaultFeedsItemCardService : IFeedsItemCardService {
    @Composable
    override fun Build(feedsItem: IFeedsVMItemStub, feedsItemCtx: FeedsItemCtx) {
        DefaultCard("Item ${feedsItemCtx.indexInList + 1}", "默认信息流卡片占位")
    }
}

@Composable
private fun SeedHeader(widget: CommonHeaderWidget) {
    val title = widget.data?.title.orEmpty().ifBlank { "KMM Base Core" }
    val desc = widget.data?.desc.orEmpty().ifBlank { "qnFramework + qnView 结构化页面 demo" }
    val actionWidgets = widget.headerList.orEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEAF3FF))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        QnText(
            text = title,
            color = Color(0xFF172033),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30f,
        )
        SpacerHeight(8.dp)
        QnText(
            text = desc,
            color = Color(0xFF5D6B82),
            fontSize = 14.sp,
            lineHeight = 21f,
        )
        if (actionWidgets.isNotEmpty()) {
            SpacerHeight(16.dp)
            actionWidgets.chunked(3).forEach { rowWidgets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowWidgets.forEach { actionWidget ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            DefaultBtnRegistry.Build(actionWidget)
                        }
                    }
                    repeat(3 - rowWidgets.size) {
                        Box(Modifier.weight(1f))
                    }
                }
                SpacerHeight(8.dp)
            }
        }
    }
}

@Composable
private fun SeedPinContent(widgetId: String) {
    val tabName = widgetId.removePrefix(SEED_PIN_CONTENT_PREFIX)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QnColor.bgPage)
            .padding(16.dp)
    ) {
        DefaultCard("品字形内容区", "当前 tab：${tabName.ifBlank { "overview" }}")
        SpacerHeight(12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DefaultCard(
                title = "qnFramework",
                desc = "提供 PageWidget、DataRepo、频道和页面协议",
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFF4D8),
            )
            DefaultCard(
                title = "qnView",
                desc = "提供 StructComposePage 与跨端渲染组件",
                modifier = Modifier.weight(1f),
                color = Color(0xFFE8F7EF),
            )
        }
        SpacerHeight(12.dp)
        DefaultCard("本地 mock 数据", "不依赖网络，先验证三端页面链路和结构化渲染闭环")
    }
}

@Composable
private fun DefaultCard(
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(color)
            .padding(14.dp)
    ) {
        QnText(
            text = title,
            color = Color(0xFF1F2937),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 22f,
        )
        SpacerHeight(6.dp)
        QnText(
            text = desc,
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            lineHeight = 19f,
        )
    }
}

@Composable
private fun DefaultWidgetPlaceholder(widget: StructWidget) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFF1F5F9))
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        QnText(
            text = widget.getWidgetType(),
            color = Color(0xFF64748B),
            fontSize = 12.sp,
        )
    }
}

private object DefaultViewServiceBridge : IViewServiceBridge {
    @Composable
    override fun createInitialPreviewDataWithPlaceholder(shareData: IKmmShareData): PostPreviewData =
        PostPreviewData(posterViews = emptyList(), posterStyles = emptyList())

    @Composable
    override fun ShowPostPreviewComponent(
        previewData: PostPreviewData,
        shareData: IKmmShareData,
        onPosterClick: (ShareResult.CONTINUE) -> Unit,
    ) = Unit

    override fun createShareChannel(shareChannel: ShareChannel): IShareChannel =
        object : IShareChannel {
            override val channel: ShareChannel = shareChannel
            override fun isSupported(): Boolean = false
            override fun share(context: com.tencent.news.core.app.IKmmContext?, shareContent: IShareContent, shareData: IKmmShareData) = Unit
        }

    override fun buildImageShareContent(
        imagePath: String,
        shareData: IKmmShareData?,
        channel: ShareChannel?,
    ): IShareContent = object : IShareContent {}

    override fun buildPageShareContent(shareData: IKmmShareData, channel: ShareChannel): IShareContent? = null
    override fun fetchShareMetaData(shareData: IKmmShareData) = Unit
    override fun createShareMetaData(shareData: IKmmShareData): Pair<String, String>? = null
    override fun defaultLoadingView(): ViewCreator? = null
    override fun defaultErrorView(): ViewCreator? = null
    override fun defaultEmptyView(): ViewCreator? = null
}

private object DefaultShareComponentRegistry : com.tencent.news.core.compose.share.IShareComponentRegistry {
    @Composable
    override fun PosterShareCard(
        feedsItem: com.tencent.news.core.list.model.IKmmFeedsItem,
        feedsList: List<com.tencent.news.core.list.model.IKmmFeedsItem>,
        onImageLoaded: (() -> Unit)?,
    ) = Unit

    @Composable
    override fun PostShareContent(
        postShareChannel: IShareChannel,
        shareData: IKmmShareData,
        feedsItem: com.tencent.news.core.list.model.IKmmFeedsItem?,
    ) = Unit
}

private object NoopBoxScope : BoxScope {
    override fun Modifier.align(alignment: Alignment): Modifier = this
    override fun Modifier.matchParentSize(): Modifier = this
}
