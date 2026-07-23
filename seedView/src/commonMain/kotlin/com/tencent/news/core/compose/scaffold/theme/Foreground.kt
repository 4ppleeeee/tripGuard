package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.ui.graphics.Color


@Stable
data class ForegroundTheme(
    val color: Color = Unspecified,
    val url: String = "",
)

/**
 * A composition local for [ForegroundTheme].
 */
internal val LocalForegroundTheme = staticCompositionLocalOf { ForegroundTheme() }

@Composable
fun WithTheme(
    lightColor: Color,
    darkColor: Color,
    content: @Composable (color: Color) -> Unit,
) {
    // 特殊场景下，可能需要强制lightMode或darkMode，但整体还是darkMode或lightMode
    // 因此，url类的应该和ColorScheme的状态保持一致，而不是听从全局的主题。
    val darkTheme = LocalColorScheme.current == DarkColorScheme
    val foreground = if (darkTheme) ForegroundTheme(darkColor) else ForegroundTheme(lightColor)
    CompositionLocalProvider(LocalForegroundTheme provides foreground) {
        content(LocalForegroundTheme.current.color)
    }
}

@Composable
fun WithTheme(
    lightColor: String,
    darkColor: String,
    content: @Composable (color: Color) -> Unit,
) {
    WithTheme(
        lightColor = lightColor.toColor(),
        darkColor = darkColor.toColor(),
        content = content
    )
}

@Composable
fun WithThemeUrl(
    lightUrl: String,
    darkUrl: String,
    content: @Composable (url: String) -> Unit,
) {
    // 特殊场景下，可能需要强制lightMode或darkMode，但整体还是darkMode或lightMode
    // 因此，url类的应该和ColorScheme的状态保持一致，而不是听从全局的主题。
    val darkTheme = LocalColorScheme.current == DarkColorScheme
    val foreground =
        if (!darkTheme) ForegroundTheme(url = lightUrl) else ForegroundTheme(url = darkUrl)
    CompositionLocalProvider(LocalForegroundTheme provides foreground) {
        content(LocalForegroundTheme.current.url)
    }
}

@Composable
private fun String.toColor(): Color {

    if (this.isEmpty()) {
        return Color.Transparent
    }

    var colorStr = if (this.startsWith("#")) {
        this.substring(1)
    } else {
        this
    }

    if (colorStr.length == 6) {
        colorStr = "FF${colorStr}"
    } else if (colorStr.length == 8) {
        colorStr = "0x${colorStr}"
    }

    // 去掉 '#' 符号
    val colorLong = colorStr.removePrefix("#").toLong(16)
    return Color(colorLong)
}
