@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.platform.pageViewWidthValue
import com.tencent.news.core.view.constants.CellSize

typealias BigScreenAdaptContent = @Composable (pageWidth: Dp, cardWidth: Dp, cardHeight: Dp) -> Unit

@Composable
fun BigScreenAdapt(
    cellSize: CellSize,
    content: BigScreenAdaptContent
) {
    val pageWidth = pageViewWidthValue()
    val cardWidth = cellSize.adaptContentWidth(pageWidth)
    val cardHeight = cellSize.adaptCellHeight(pageWidth)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(cellSize.adaptAspectRatio(pageWidth))
    ) {
        content(pageWidth.dp, cardWidth.dp, cardHeight.dp)
    }

}