package com.tencent.news.markdown.model

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.core.base.Size

internal class NoOpImageTransformerImpl : ImageTransformer {

    @Composable
    override fun transform(link: String, containerSize: Size?, inline: Boolean): ImageData {
        return ImageData(
            painter = rememberAsyncImagePainter(link),
        )
    }

    @Composable
    override fun intrinsicSize(painter: Painter): Size {
        return Size(100F, 100F)
    }
}