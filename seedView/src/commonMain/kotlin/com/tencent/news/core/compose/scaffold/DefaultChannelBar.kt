package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.material3.TabRowDefaults
import com.tencent.kuikly.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.modifiers.borderRadius
import com.tencent.news.core.compose.platform.statusBarHeight
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.page.model.IWidgetChannelBarVM

@Composable
fun DefaultChannelBar(
    vm: IWidgetChannelBarVM,
    pagerState: PagerState,
) {
    val adaptHeight = if (vm.adaptStatusBar) statusBarHeight() else 0.dp
    StructChannelBar(
        vm = vm,
        pagerState = pagerState,
        barModifier = Modifier.fillMaxWidth().height(40.dp + adaptHeight)
            .padding(start = 40.dp, end = 40.dp, top = adaptHeight),
        barStyle = StructChannelBarStyle.TAB,
        tabSpace = 25.dp,
        barIndicator = channelBarIndicator(pagerState, 25.dp),
    ) { isSelected, tab, index ->
        QnText(
            text = tab.channel_name,
            modifier = Modifier.padding(bottom = 5.dp),
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) QnColor.t1 else QnColor.t2,
        )
    }
}

@Composable
fun channelBarIndicator(
    pagerState: PagerState,
    tabSpace: Dp,
    width: Dp = 16.dp,
    height: Dp = 3.dp,
    color: Color = QnColor.bNormal
): BarIndicator = { tabPositions ->
    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    if (tabPositions.isNotEmpty() && selectedTabIndex in tabPositions.indices) {
        // 所有 tab 的 TabPosition.width 都包含 tabSpace，统一减去 tabSpace 得到内容宽度
        val horizontalPadding = (tabPositions[selectedTabIndex].width - tabSpace - width) / 2

        TabRowDefaults.SecondaryIndicator(
            Modifier
                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                .width(width).height(height)
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding + tabSpace,
                )
                .borderRadius(100.dp),
            color = color,
        )
    }
}