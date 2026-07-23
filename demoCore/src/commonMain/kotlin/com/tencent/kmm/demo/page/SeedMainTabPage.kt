package com.tencent.kmm.demo.page

import androidx.compose.runtime.Composable
import com.tencent.kmm.demo.home.DemoRoutes
import com.tencent.kuikly.core.annotations.Page
import com.tencent.news.core.compose.page.StructComposePage
import com.tencent.news.core.compose.scaffold.ComposePage
import com.tencent.news.core.list.api.SimpleLocalDataRepo
import com.tencent.news.core.list.model.ChannelShowType
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.CommonHeaderWidget
import com.tencent.news.core.page.model.CommonTitleBarWidget
import com.tencent.news.core.page.model.HeaderWidgetData
import com.tencent.news.core.page.model.PagerWidget
import com.tencent.news.core.page.model.StructPageConfig
import com.tencent.news.core.page.model.StructPageLoadingViewType
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.page.model.StructSimpleWidget
import com.tencent.news.core.view.setup.setupDefaultViewService
import com.tencent.news.qnchannel.api.IChannelInfo

private const val DEFAULT_HOST = "https://localhost"

@Page(name = DemoRoutes.MAIN_TAB)
class SeedMainTabPage : ComposePage() {

    override fun enableGlobalDebugFloatingEntry(): Boolean = false

    @Composable
    override fun OnSetContent() {
        setupDefaultViewService()
        StructComposePage(
            pageWidget = { createSeedMainTabPageWidget() },
            pageFlow = pageFlow,
            key = DemoRoutes.MAIN_TAB,
        )
    }
}

fun createSeedMainTabPageWidget(): StructPageWidget2 =
    StructPageWidget2(
        pageConfig = StructPageConfig(
            dataRepo = SimpleLocalDataRepo { buildSeedMainTabContent() },
            defaultChannelInfo = seedChannelInfo("seed-main", "首页"),
            defaultRequestHost = DEFAULT_HOST,
            enableFooter = false,
            enableHeader = false,
            loadingViewType = StructPageLoadingViewType.EMPTY,
        )
    ).apply {
        titleBar = CommonTitleBarWidget.create(
            title = "KMM Base Core",
            isBarIconDark = true,
        ).also { it.ui.hideBackBtn = true }
        header = seedHeader()
        pager = seedPager()
    }

private fun StructPageWidget.buildSeedMainTabContent() {
    titleBar = CommonTitleBarWidget.create(
        title = "KMM Base Core",
        isBarIconDark = true,
    ).also { it.ui.hideBackBtn = true }
    header = seedHeader()
    pager = seedPager()
}

private fun seedHeader(): CommonHeaderWidget =
    CommonHeaderWidget().apply {
        data = HeaderWidgetData().apply {
            title = "KMM Base Core"
            desc = "使用 qnFramework 的 PageWidget/DataRepo，走 qnView 的 StructComposePage 渲染"
        }
    }

private fun seedPager(): PagerWidget =
    PagerWidget.create(
        seedChannel("overview", "总览"),
        seedChannel("android", "Android"),
        seedChannel("ios", "iOS"),
    ).apply {
        channelBar?.action?.forceShowChannelBar = true
    }

private fun seedChannel(channelKey: String, channelName: String): ChannelWidget =
    ChannelWidget.create(
        channelId = channelKey,
        channelName = channelName,
        showType = ChannelShowType.COMMON_LIST,
    ).apply {
        status.enableFooter = false
        status.enableHeader = false
        empty = object : StructSimpleWidget() {}.apply {
            widget_id = "seed_pin_content_$channelKey"
        }
    }

private fun seedChannelInfo(channelKey: String, channelName: String): IChannelInfo =
    IChannelInfo.createDefault(channelKey, channelName)
