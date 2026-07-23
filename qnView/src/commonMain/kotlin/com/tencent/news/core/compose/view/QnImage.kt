package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.Image
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.graphics.DefaultAlpha
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.news.core.resources.Res

/**
 * 图片组件，若[modifier]用[com.tencent.kuikly.compose.ui.unit.Dp]指定了具体宽高则跟随字号缩放，否则不缩放
 */
@Composable
fun QnImage(
    painter: Painter,
    contentDescription: String? = "图片", // 无障碍模式，会阅读这个文案；尽量填写
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

@Composable
fun QnImage(
    url: String,
    contentDescription: String? = "图片", // 无障碍模式，会阅读这个文案；尽量填写
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    QnImage(
        rememberAsyncImagePainter(
            model = url,
            placeholder = Res.drawable.bgBlock,
            error = Res.drawable.bgBlock
        ),
        contentDescription,
        modifier,
        alignment,
        contentScale,
        alpha,
        colorFilter
    )
}