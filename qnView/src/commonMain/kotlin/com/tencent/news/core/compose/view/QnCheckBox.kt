package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.modifiers.Border
import com.tencent.news.core.compose.scaffold.modifiers.border
import com.tencent.news.core.compose.scaffold.modifiers.borderRadius
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.compose.platform.QnIconFont
import com.tencent.news.core.compose.scaffold.modifiers.debugBackground
import com.tencent.news.core.compose.scaffold.theme.QNTheme

@Composable
fun Modifier.defaultCheckBoxStyle(): Modifier {
    return this.border(
        Border(
            lineWidth = 0.8.dp,
            color = QNTheme.colorScheme.backIconColor,
            lineStyle = BorderStyle.SOLID
        )
    ).borderRadius(1.dp)
}

@Composable
fun QnCheckBox(
    modifier: Modifier = Modifier,
    iconFontModifier: Modifier = Modifier.size(8.dp).margin(bottom = 1.dp),
    textStyle: TextStyle = TextStyle(
        color = QNTheme.colorScheme.backIconColor,
        fontSize = 8.sp,
        fontWeight = FontWeight.W700
    ),
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled, onClick = {
                if (!enabled) {
                    return@clickable
                }
                onCheckedChange?.invoke(!checked)
            }),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            QnIconFont(
                name = IconFont.XW_DUIGOU,
                textStyle = textStyle,
                modifier = iconFontModifier
            )
        }
    }
}

@Composable
fun QnImageCheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)?,
    enabled: Boolean = true,
    checkmarkSrc: Painter,
    uncheckmarkSrc: Painter
) {
    Box(
        modifier = modifier.wrapContentSize()
            .debugBackground(Color.Blue)
            .clickable(enabled = enabled, onClick = {
                if (!enabled) {
                    return@clickable
                }
                onCheckedChange?.invoke(!checked)
            })
    ) {
        val checkMark = if (checked) checkmarkSrc else uncheckmarkSrc
        QnImage(
            painter = checkMark,
            contentScale = ContentScale.FillBounds,
            contentDescription = null,
            modifier = Modifier.size(22.dp).padding(4.dp)
        )
    }
}