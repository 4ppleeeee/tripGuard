package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.foundation.layout.wrapContentWidth
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.material3.ScrollableTabRow
import com.tencent.kuikly.compose.material3.Tab
import com.tencent.kuikly.compose.material3.TabPosition
import com.tencent.kuikly.compose.material3.TabRow
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.scaffold.modifiers.dtElement
import com.tencent.news.core.compose.scaffold.modifiers.QnViewDtElementIds
import com.tencent.news.core.dt.constants.DtParamKey
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.page.model.ChannelBarItem
import com.tencent.news.core.page.model.IWidgetChannelBarVM

typealias BarItemContent = @Composable (isSelected: Boolean, tab: ChannelBarItem, index: Int) -> Unit
typealias BarIndicator = @Composable (tabPositions: List<TabPosition>) -> Unit
typealias ItemDtElement = Modifier.(tabId: String) -> Modifier
typealias ItemModifier = (isSelected: Boolean, tab: ChannelBarItem, index: Int) -> Modifier

// 一级频道导航（一般使用参数：nav_item_id）目前有些不规范的底层页也用的这个
// todo 【注意】：底层页不应该用这个，只有一些遗留老页面用了
fun dtItemNavTab(): ItemDtElement = { tabId ->
    dtElement(
        elementId = QnViewDtElementIds.EM_ITEM_NAV,
        elementParams = mapOf(DtParamKey.NAV_ITEM_ID to tabId),
        enableExposure = true,
        enableClick = true
    )
}

// 底层页中的导航（事件/tag等等，一般使用参数：nav_id）
// todo 【注意】：新增底层页，都应该用这个
fun dtItemSubNav(): ItemDtElement = { tabId ->
    dtElement(
        elementId = QnViewDtElementIds.EM_ITEM_SUB_NAV,
        identifier = tabId,
        elementParams = mapOf(DtParamKey.NAV_ID to tabId),
    )
}

// 导航不上报大同
fun dtNotReport(): ItemDtElement = { this }

fun defaultBarModifier(): Modifier = Modifier.wrapContentWidth().height(33.dp).margin(bottom = 8.dp)
fun defaultItemModifier(): ItemModifier = { _, _, _ -> Modifier.wrapContentSize() }

enum class StructChannelBarStyle {
    SCROLL, // 可以滚动的导航条（一般用于大量tab）
    TAB,    // 均分，不可滚动的导航条（一般用于有限个tab）
}

/**
 * 【通用组件】多tab导航条：
 * 1. 已处理好与 IWidgetChannelBarVM 的flow绑定
 * 2. 可以通过 vm.changeSelection 切换 pager 选中态
 * 3. 在 itemContent 自定义导航样式
 * 4. 通过 itemDtElement 指定大同上报（默认[dtItemSubNav]）如果不想报，用：[dtNotReport]
 */
@Composable
fun StructChannelBar(
    vm: IWidgetChannelBarVM,
    pagerState: PagerState,
    edgePadding: Dp = 16.dp,    // 导航条左右边距
    tabSpace: Dp = 8.dp,        // 导航条item间距（已经处理了，最后一个item的end不添加）
    containerColor: Color = Color.Transparent,          // 默认没有背景色
    barModifier: Modifier = defaultBarModifier(),       // 整个导航条的样式
    barIndicator: BarIndicator = {},                    // 游标样式
    barStyle: StructChannelBarStyle = StructChannelBarStyle.SCROLL, // 导航条样式
    itemModifier: ItemModifier = defaultItemModifier(), // 导航item样式（一般不用动这个，在itemContent定义就行）
    itemDtElement: ItemDtElement = dtItemSubNav(),      // 默认按底层页子tab上报
    onTabClickIntercept: (Int) -> Boolean = { false },
    itemContent: BarItemContent,
) {
    val items = vm.items.takeIfNotEmpty() ?: return

    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    // 监听 pager 状态变化，同步到 VM
    WatchPagerIndexWithVM(vm, pagerState)

    val tabContent = @Composable {
        items.forEachIndexed { index, tab ->
            val isSelected = selectedTabIndex == index
            Tab(
                selected = isSelected,
                modifier = itemModifier(isSelected, tab, index)
                    .margin(end = tabSpace)
                    .itemDtElement(tab.channel_id),
                onClick = {
                    if (!onTabClickIntercept(index)) {
                        vm.changeSelection(index)
                    }
                },
            ) {
                val isSelected = selectedTabIndex == index
                itemContent(isSelected, tab, index)
            }
        }
    }

    when (barStyle) {
        StructChannelBarStyle.SCROLL -> ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = barIndicator,
            divider = {},
            edgePadding = edgePadding,
            containerColor = containerColor,
            modifier = barModifier,
        ) {
            tabContent()
        }

        StructChannelBarStyle.TAB -> TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = barIndicator,
            divider = {},
            containerColor = containerColor,
            modifier = barModifier,
        ) {
            tabContent()
        }
    }

}

@Composable
fun WatchPagerIndexWithVM(vm: IWidgetChannelBarVM, pagerState: PagerState) {
    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    // 记录上一次同步到 VM 的索引，避免重复调用
    var lastSyncedToVM by remember { mutableIntStateOf(-1) }

    // Pager 变化 -> 同步到 VM
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != lastSyncedToVM) {
            lastSyncedToVM = selectedTabIndex
            vm.changeSelection(selectedTabIndex)
        }
    }

    // VM 变化 -> 同步到 Pager
    val targetIndex by vm.selectedIndex.collectAsState()
    LaunchedEffect(targetIndex) {
        if (targetIndex != pagerState.currentPage) {
            // 更新标志位，避免 scrollToPage 触发的 currentPage 变化再次调用 VM
            lastSyncedToVM = targetIndex
            pagerState.scrollToPage(targetIndex)
        }
    }
}
