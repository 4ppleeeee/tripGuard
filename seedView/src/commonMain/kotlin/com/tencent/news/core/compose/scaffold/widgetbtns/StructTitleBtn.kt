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
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.qa.view.AvatarView
import com.tencent.news.core.compose.scaffold.registry.LocalHeaderCollapseStatus
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.page.model.StructWidgetRegistry
import com.tencent.news.core.page.model.StructWidgetType
import com.tencent.news.core.page.model.TitleBtnWidget

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

        val titleFontSize = if (data.fontSize > 0f) data.fontSize.sp else 16.sp
        QnText(
            text = data.title,
            color = getTitleBarWidgetColor(
                isHeaderCollapsed = isHeaderCollapsed,
                defaultColor = currentTitleBarTheme.titleTextColor
            ),
            fontSize = titleFontSize,
            fontWeight = FontWeight(500),
//            modifier = Modifier.fillMaxWidth(0.5f),
            maxLines = 1
        )
    }
}