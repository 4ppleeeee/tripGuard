package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.theme.DarkColorScheme
import com.tencent.news.core.compose.scaffold.theme.LocalColorScheme
import com.tencent.news.core.page.model.GradientBgWidget

// 【通用】渐变背景组件（从上到下线性渐变）
@Composable
fun GradientBgView(widget: GradientBgWidget) {
    val isDark = LocalColorScheme.current == DarkColorScheme
    val startColor = if (isDark) widget.nightStartColor else widget.dayStartColor
    val endColor = if (isDark) widget.nightEndColor else widget.dayEndColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(widget.fixHeight.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor.toComposeColor(), endColor.toComposeColor())
                )
            )
    )
}

/**
 * 将颜色字符串（如 "#FF3377FF" 或 "#3377FF"）转换为 Compose Color
 */
private fun String.toComposeColor(): Color {
    if (this.isEmpty()) return Color.Transparent

    var colorStr = this.removePrefix("#")
    if (colorStr.length == 6) {
        colorStr = "FF$colorStr"
    }
    return Color(colorStr.toLong(16))
}
