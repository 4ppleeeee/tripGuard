package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.aspectRatio
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.qa.view.AvatarView
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatus
import com.tencent.news.core.compose.scaffold.theme.DarkColorScheme
import com.tencent.news.core.compose.scaffold.theme.LocalColorScheme
import com.tencent.news.core.compose.scaffold.theme.lantingheiFamily
import com.tencent.news.core.compose.utils.parseValidColor
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.page.model.StructColor
import com.tencent.news.core.page.model.StructWidgetRegistry
import com.tencent.news.core.page.model.StructWidgetType
import com.tencent.news.core.page.model.TitleBtnShowType
import com.tencent.news.core.page.model.TitleBtnWidget
import com.tencent.news.core.page.model.TitleBtnWidgetData

@Composable
@StructWidgetRegistry(StructWidgetType.TITLE_BTN)
fun TitleBtn(widget: TitleBtnWidget?) {
    val data = widget?.data ?: return
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (data.iconUrl.isNotNullOrEmpty()) {
            if (data.isAvatar) {
                AvatarView(
                    modifier = Modifier
                        .size(24.dp),
                    avatarUrl = data.iconUrl ?: "",
                    vipIcon = data.flagUrl,
                    showBorder = false
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                QnImage(
                    painter = rememberAsyncImagePainter(data.iconUrl),
                    contentDescription = null,
                    // 指定图片填充宽度、高度自适应
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .width(width = 20.dp)
                        .height(height = 20.dp)
                        .fillMaxWidth()
                        // 注意：这里需要根据图片宽高比动态设置宽高比，不然图片展示不出来
                        .aspectRatio(1.0f)
                )
            }
        }

        val isHeaderCollapsed by LocalHeaderCollapseStatus.current

        StructTitleText(
            data = data,
            color = getTitleBarWidgetColor(
                isHeaderCollapsed = isHeaderCollapsed,
                defaultColor = currentTitleBarTheme.titleTextColor
            )
        )
    }
}

@Composable
private fun StructTitleText(data: TitleBtnWidgetData, color: Color) {
    val fontFamily = data.getFontFamily()
    // 夜间模式，若设置了isNightUseNormalTextColor，则使用普通颜色
    val useNormalTextColor = LocalColorScheme.current == DarkColorScheme && data.isNightUseNormalTextColor
    val normalTextColor = if (useNormalTextColor) data.normalTextColor?.currentColor() else null
    val gradientColors = if (useNormalTextColor) {
        null
    } else {
        data.gradientColors?.mapNotNull { it.currentColor() }
            ?.takeIf { it.size >= MIN_GRADIENT_COLOR_COUNT }
    }
    val textStyle = gradientColors?.let {
        TextStyle(
            brush = Brush.linearGradient(
                colors = it,
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, 0f)
            )
        )
    }

    QnText(
        text = data.title,
        color = if (textStyle == null) normalTextColor ?: color else Color.Unspecified,
        fontSize = data.fontSize.sp,
        fontWeight = FontWeight(500),
        fontFamily = fontFamily,
//            modifier = Modifier.fillMaxWidth(0.5f),
        maxLines = 1,
        style = textStyle
    )
}

@Composable
private fun StructColor.currentColor(): Color? {
    val color = if (LocalColorScheme.current == DarkColorScheme) nightColor else dayColor
    return color.parseValidColor()
}

private fun TitleBtnWidgetData.getFontFamily(): FontFamily? {
    return when (fontType) {
        TitleBtnShowType.LANTING_FONT -> lantingheiFamily
        else -> null
    }
}

private const val MIN_GRADIENT_COLOR_COUNT = 2