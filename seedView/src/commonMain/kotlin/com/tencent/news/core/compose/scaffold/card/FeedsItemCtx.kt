package com.tencent.news.core.compose.scaffold.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.registry.LocalStructPagePagerIndex
import com.tencent.news.core.compose.scaffold.registry.LocalStructVerticalPagerIndex
import com.tencent.news.core.constants.INVALID_NUM

@Suppress("RedundantConstructorKeyword")
data class FeedsItemCtx constructor(
    // Cell在列表中的位置
    val indexInList: Int,
    val pagerIndex: Int = 0, // 当前所在子tab的index
    val listSize: Int = INVALID_NUM, // 列表内容总个数（不包含footer）
    val isFirstItem: Boolean = indexInList == 0,            // 判断列表收尾cell，可做样式逻辑
    val isLastItem: Boolean = indexInList == listSize - 1,  // 判断列表收尾cell，可做样式逻辑
    val dislikeHandler: IDislikeHandler? = null,
    val contentPadding: Dp = 0.dp
)


/**
 * 通过 pager 当前页索引判断当前 cell 是否被选中
 */
@Composable
fun FeedsItemCtx.checkCellSelected(): Boolean {
    val pagePagerIndex by LocalStructPagePagerIndex.current
    val subVerticalPagerIndex by LocalStructVerticalPagerIndex.current
    return pagePagerIndex == pagerIndex &&
            subVerticalPagerIndex == indexInList
}

@Composable
fun FeedsItemCtx.checkPageSelected(): Boolean {
    val pagePagerIndex by LocalStructPagePagerIndex.current
    return pagePagerIndex == pagerIndex
}