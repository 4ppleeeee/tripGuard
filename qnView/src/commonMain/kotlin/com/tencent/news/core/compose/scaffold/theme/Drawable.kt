package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.resources.DrawableResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.resources.painterResource
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.core.base.attr.ImageUri


@Immutable
data class DrawableTheme(
    val drawable: String,
)

@OptIn(InternalResourceApi::class)
@Composable
fun drawable(name: String): Lazy<Painter> {
    return lazyOf(
        painterResource(
            DrawableResource(
                ImageUri.pageAssets(name).toUrl(LocalDrawable.current.drawable)
            )
        )
    )
}

@OptIn(InternalResourceApi::class)
@Composable
fun lightDrawable(name: String): Lazy<Painter> {
    return lazyOf(
        painterResource(
            DrawableResource(
                ImageUri.pageAssets(name).toUrl(LightDrawableTheme.drawable)
            )
        )
    )
}


val LightDrawableTheme = DrawableTheme("drawable")

val DarkDrawableTheme = DrawableTheme("dark-drawable")

/**
 * A composition local for [BackgroundTheme].
 */
val LocalDrawable = staticCompositionLocalOf { LightDrawableTheme }

