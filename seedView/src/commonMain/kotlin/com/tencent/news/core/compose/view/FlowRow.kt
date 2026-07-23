package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.ExperimentalLayoutApi
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.Layout
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


//    var listWidth by remember { mutableStateOf(1F) }
//    Column(modifier = modifier.onSizeChanged { listWidth = it.width }) {
//        LazyVerticalGrid(
//            modifier = Modifier.fillMaxWidth().weight(1F),
//            columns = GridCells.Fixed(maxItemsInEachRow),
//            listWidth = listWidth,
//            columnsSpacing = 10.dp,
//            rowsSpacing = 10.dp,
//        ) {
//            items(list) { itemContent(it) }
//        }
//    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FlowRow2(
    modifier: Modifier = Modifier,
    list: List<T>,
    maxItemsInEachRow: Int = 4,
    minItemsInEachRow: Int = 1,
    horizontalSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 10.dp,
    item: @Composable (item: T) -> Unit,
) {

    if (list.isEmpty()) return

    val density = LocalDensity.current

    fun calculateItemWidth(rowWidth: Int, itemCount: Int): Int = with(density) {
        return (rowWidth - (itemCount - 1) * horizontalSpacing.roundToPx()) / itemCount
    }

    Layout(
        modifier = modifier,
        content = {
            list.forEach { item(it) }
        },
        measurePolicy = { measurables, constraints ->

            // 第一次测量：得到所有子View的最小宽高
            val widthOfEachItem = measurables.map { measurable ->
                measurable.minIntrinsicWidth(0)
            }.maxOf { it }

            // 每行最多放置的item个数
            // val itemsInEachRow = constraints.maxWidth / widthOfEachItem
            // itemsInEachRow * widthOfEachItem + (itemsInEachRow - 1) * horizontalSpacing <= maxWidth
            val horizontalSpacingPx = horizontalSpacing.roundToPx()
            val itemsInEachRow =
                (constraints.maxWidth + horizontalSpacingPx) / (widthOfEachItem + horizontalSpacingPx)

            val actualItemInEachRow = max(min(maxItemsInEachRow, itemsInEachRow), minItemsInEachRow)

            // 重新计算每一每个item的宽度，撑满整行
            val recalculatedMinWidth = if (actualItemInEachRow <= 1) {
                constraints.maxWidth
            } else {
                calculateItemWidth(constraints.maxWidth, actualItemInEachRow)
            }

            // 第二次测量：让行内撑满
            val placeables = measurables.map { measurable ->
                measurable.measure(
                    constraints.copy(
                        minWidth = recalculatedMinWidth,
                        maxWidth = recalculatedMinWidth
                    )
                )
            }

            // 计算高度
            val actualRows = ceil(placeables.size * 1F / actualItemInEachRow).toInt()
            val actualHeight =
                actualRows * placeables.first().height + (actualRows - 1) * verticalSpacing.roundToPx()

            // 布局
            layout(constraints.maxWidth, actualHeight) {
                var x = 0
                var y = 0
                placeables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + horizontalSpacingPx
                    // 换行
                    if ((index + 1) % actualItemInEachRow == 0) {
                        x = 0
                        y += placeable.height + verticalSpacing.roundToPx()
                    }
                }
            }
        }
    )
}

