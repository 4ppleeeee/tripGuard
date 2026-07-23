package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.material3.Surface
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp

internal val Unspecified: Color get() = Color.Unspecified

@Immutable
internal data class BackgroundTheme(
    val color: Color = Unspecified,
    // unused so far
    val tonalElevation: Dp = 0.dp,
)

/**
 * A composition local for [BackgroundTheme].
 */
internal val LocalBackgroundTheme = staticCompositionLocalOf { BackgroundTheme() }

/**
 * The main background for the app.
 * Uses [LocalBackgroundTheme] to set the color and tonal elevation of a [Box].
 *
 * @param modifier Modifier to be applied to the background.
 * @param content The background content.
 */
@Composable
fun QnAppBackground(
    modifier: Modifier = Modifier,
    color: Color = LocalBackgroundTheme.current.color,
    tonalElevation: Dp = LocalBackgroundTheme.current.tonalElevation,
    content: @Composable () -> Unit,
) {
    Surface(
        color = if (color == Unspecified) Color.Transparent else color,
        modifier = modifier.fillMaxSize(),
    ) {
        content()
    }
}

