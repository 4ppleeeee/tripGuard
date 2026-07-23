package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.layout.wrapContentWidth
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.app.constants.IIconFont
import com.tencent.news.core.compose.platform.QnIconFont
import com.tencent.news.core.compose.scaffold.modifiers.padding2
import com.tencent.news.core.compose.scaffold.theme.LightColorScheme
import com.tencent.news.core.compose.scaffold.theme.QNTheme

@Composable
fun ActionBubble(
    actions: List<ActionItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .background(LightColorScheme.t1)
            .padding2(hor = 21.dp, ver = 12.dp)
            .clip(QNTheme.shape.extraSmall)
//            .background(LightColoScheme.t1)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEachIndexed { index, action ->
            ActionItem(item = action)

            if (index < actions.size - 1) {
                Spacer(modifier = Modifier.width(36.dp))
            }
        }
    }
}

@Composable
internal fun ActionItem(
    item: ActionItem,
    modifier: Modifier = Modifier
) {
    if (item.isVertical) {
        Column(
            modifier = modifier.clickable { item.onClick() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            QnIconFont(
                name = item.iconName,
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = item.iconColor ?: LightColorScheme.t4
                ),
                modifier = modifier.clickable { item.onClick() },
            )

            if (item.text != null) {
                Spacer(modifier = Modifier.height(4.dp))
                QnText(
                    text = item.text,
                    color = item.textColor ?: LightColorScheme.t4,
                    fontSize = 12.sp,
                    modifier = modifier.clickable { item.onClick() },
                )
            }
        }
    } else {
        Row(
            modifier = modifier.clickable { item.onClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            QnIconFont(
                name = item.iconName,
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = item.iconColor ?: LightColorScheme.t4
                ),
                modifier = modifier.clickable { item.onClick() },
            )

            if (item.text != null) {
                Spacer(modifier = Modifier.width(6.dp))
                QnText(
                    text = item.text,
                    color = item.textColor ?: LightColorScheme.t4,
                    fontSize = 12.sp,
                    modifier = modifier.clickable { item.onClick() },
                )
            }
        }
    }
}

data class ActionItem(
    val iconName: IIconFont,
    val text: String? = null,
    val iconColor: Color? = null,
    val textColor: Color? = null,
    val isVertical: Boolean = false,
    val onClick: () -> Unit
)
