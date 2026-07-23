package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.toArgb
import com.tencent.news.core.platform.api.PaletteCache
import com.tencent.news.core.platform.api.PaletteParam
import com.tencent.news.core.platform.api.resManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PaletteVerticalMask(
    bgImg: String,
    modifier: Modifier = Modifier,
    startColor: Color = Color.Transparent,
    startY: Float = 0f,
    endY: Float = Float.POSITIVE_INFINITY
) {
    PaletteCustomMask(
        bgImg = bgImg
    ) {
        Box(
            modifier = modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            startColor,
                            it
                        ),
                        startY = startY,
                        endY = endY
                    )
                )
        )
    }
}

@Composable
fun PaletteCustomMask(
    bgImg: String,
    uniqueId: Int = 0,
    defaultColor: Color? = null,
    paletteParam: PaletteParam? = null,
    content: @Composable (Color) -> Unit
) {
    var overlayColor: Int? by remember { mutableStateOf(null) }
    LaunchedEffect(bgImg, uniqueId) {
        bgImg.let { bg ->
            if (paletteParam == null) {
                return@LaunchedEffect
            }
            val color = PaletteCache.get(bg, paletteParam)
            if (color != null) {
                overlayColor = color
            } else {
                withContext(Dispatchers.Default) {
                    resManager()?.getPaletteColor(
                        imageUrl = bg,
                        param = paletteParam,
                        defaultColor = defaultColor?.toArgb()
                    ) { color ->
                        overlayColor = color
                        PaletteCache.put(bg, paletteParam, color)
                    }
                }
            }
        }
    }
    if (overlayColor == null) {
        defaultColor?.let {
            content.invoke(defaultColor)
        }
    } else {
        overlayColor?.let { color ->
            // 渐变蒙层
            content.invoke(Color(color))
        }
    }
}