package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.platform.fdp
import com.tencent.news.core.compose.scaffold.card.FeedsCardHorPadding
import com.tencent.news.core.list.model.Divider
import com.tencent.news.core.list.model.Dividers

@Composable
fun ListDivider(divider: Divider?, color: Color = QNTheme.colorScheme.lineFine) {
    Divider(divider, color)
}

@Composable
fun NoneDivider(color: Color = QNTheme.colorScheme.lineFine) {
    Divider(Dividers.NONE, color)
}

@Composable
fun ThinDivider(color: Color = QNTheme.colorScheme.lineFine) {
    Divider(Dividers.THIN, color)
}

@Composable
fun TransparentWideDivider(color: Color = QNTheme.colorScheme.transparent) {
    Divider(Dividers.WIDE, color)
}

@Composable
fun FullWidthThinDivider(color: Color = QNTheme.colorScheme.lineFine) {
    Divider(Dividers.THIN.copy(isFullWidth = true), color)
}

@Composable
fun FullWidthDivider(height: Dp, color: Color = QNTheme.colorScheme.lineFine) {
    Divider(Dividers.THIN.copy(isFullWidth = true, height = height.value), color)
}

@Composable
fun WideDivider(color: Color = QNTheme.colorScheme.lineFine) {
    Divider(Dividers.WIDE, color)
}

@Composable
private fun Divider(divider: Divider?, color: Color) {
    divider ?: return
    if (divider == Dividers.NONE) return

    val margin = if (divider.isFullWidth) 0.fdp else FeedsCardHorPadding

    Spacer(
        modifier = Modifier
            .margin(start = margin, end = margin)
            .fillMaxWidth()
            .height(divider.height.dp)
            .background(color)
    )
}


