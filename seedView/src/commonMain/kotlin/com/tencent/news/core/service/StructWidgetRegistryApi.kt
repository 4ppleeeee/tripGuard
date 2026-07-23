package com.tencent.news.core.service

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.news.core.compose.scaffold.StructPageScrollScaffold
import com.tencent.news.core.extension.IStructWidgetRegistryDoc
import com.tencent.news.core.page.model.BottomBarWidget
import com.tencent.news.core.page.model.ChannelBarWidget
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.CommonTitleBarWidget
import com.tencent.news.core.page.model.HeaderWidget
import com.tencent.news.core.page.model.StructWidget


// 页面组件统一注册工厂接口

// 浮层、弹窗：
interface IStructLayerRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(boxScope: BoxScope, widget: StructWidget)
}

// 页面顶部导航
interface IStructTitleBarRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(titleBarWidget: CommonTitleBarWidget)
}

// 页面头部
interface IStructHeaderRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(headerWidget: HeaderWidget?)
}

// 页面底部导航条
interface IStructBottomBarRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(bottomWidget: BottomBarWidget?)
}

// 多tab导航条
interface IStructChannelBarRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(
        pagerState: PagerState,
        channelBarWidget: ChannelBarWidget,
        onChannelSwitchIntercept: ((Int) -> Boolean)?,
    )
}

// 悬停区组件
interface IStructHangingRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(hangingWidget: StructWidget)
}

// 各种按钮（可配合 TitleBar、BottomBar、Layer使用）
interface IStructBtnRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(btnWidget: StructWidget)
}

// 频道、子tab
interface IStructChannelRegistry : IStructWidgetRegistryDoc {
    @Composable
    fun Build(channelWidget: ChannelWidget, scrollScaffold: StructPageScrollScaffold)
}