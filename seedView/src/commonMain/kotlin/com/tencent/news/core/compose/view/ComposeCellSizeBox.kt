package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.scaffold.modifiers.height
import com.tencent.news.core.compose.platform.pageViewWidthValue
import com.tencent.news.core.view.constants.CellSize

private typealias cellContent =
        @Composable()
            () -> Unit

/**
 * 根据 CellSize 提供初始尺寸的透明容器
 * 用途：在创建 Compose 视图时，确保容器有初始尺寸，避免布局异常
 */
@Composable
fun ComposeCellSizeBox(
    cellSize: CellSize,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: cellContent
) {
    val pageWidth = pageViewWidthValue()

    if (cellSize.aspectRatio > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(cellSize.adaptAspectRatio(pageWidth)),
            contentAlignment = contentAlignment
        ) {
            content()
        }
    } else if (cellSize.initHeightInDp > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(cellSize.initHeightInDp),
            contentAlignment = contentAlignment
        ) {
            content()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth(),
            contentAlignment = contentAlignment
        ) {
            content()
        }
    }
}