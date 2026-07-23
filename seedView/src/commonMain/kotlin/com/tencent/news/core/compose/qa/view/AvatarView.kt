package com.tencent.news.core.compose.qa.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.extension.isNotNullOrEmpty

@Composable
fun AvatarView(
    modifier: Modifier = Modifier,
    avatarUrl: String,
    vipIcon: String? = null,
    showBorder: Boolean = true
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            val borderWidth = if (showBorder) 1.dp else 0.dp
            QnImage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(QNTheme.colorScheme.lineStroke)
                    .clip(CircleShape) // 圆形裁剪
                    .border(borderWidth, QNTheme.colorScheme.lineStroke, CircleShape), // 1像素边框,
                painter = rememberAsyncImagePainter(avatarUrl),
                contentDescription = "头像",
                contentScale = ContentScale.Crop
            )
            if (vipIcon.isNotNullOrEmpty()) {
                QnImage(
                    modifier = Modifier
                        .fillMaxSize(0.33f)
                        .background(Color.Transparent),
                    painter = rememberAsyncImagePainter(vipIcon),
                    contentDescription = "作者标签",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

}