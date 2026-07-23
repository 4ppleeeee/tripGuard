package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.resources.FontResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.compose.platform.NormalDensityScale
import com.tencent.news.core.compose.platform.pageViewHeight
import com.tencent.news.core.compose.platform.pageViewWidth
import com.tencent.news.core.compose.scaffold.registry.LocalScreenshot
import com.tencent.news.core.compose.scaffold.skin.PageSkin
import com.tencent.news.core.compose.scaffold.skin.StructPageSkinTheme
import com.tencent.news.core.compose.view.ScreenshotState
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.list.trace.ComposeViewLog
import com.tencent.news.core.platform.api.appStatus

@Immutable
data class UiMode(
    // 暗色主题
    val isDarkTheme: Boolean,
    // 文字缩放比例
    val textScaleLevel: DensityScaleGradient = DensityScaleGradient.L1,
    // 自定义字体名字
    val textFontFamily: FontResource? = null,
)

val DefaultUiMode
    get() = UiMode(
        isDarkTheme = appStatus().isNightMode(),
        textScaleLevel = appStatus().currentTextScaleGradient(),
        textFontFamily = appStatus().getDefaultFontFamily().takeIfNotEmpty()?.let {
            @OptIn(InternalResourceApi::class)
            FontResource(it)
        }
    )

val LocalUiMode = staticCompositionLocalOf { DefaultUiMode }

@Composable
fun isAppInDarkTheme(): Boolean = LocalUiMode.current.isDarkTheme

/**
 * Light background theme
 */
private val LightBackgroundTheme = BackgroundTheme(color = LightColorScheme.bgPage)

/**
 * Dark background theme
 */
private val DarkBackgroundTheme = BackgroundTheme(color = DarkColorScheme.bgPage)

val QnColor: ColorScheme
    @Composable
    get() = QNTheme.colorScheme

val QnShape: Shapes
    @Composable
    get() = QNTheme.shape

val QnSkin: PageSkin?
    @Composable
    get() = StructPageSkinTheme.skinColor

object QNTheme {

    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val shape: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}

/**
 * App主题，仅应该再[ComposePage.setContentCompat]中使用
 */
@Composable
fun QNAppTheme(content: @Composable BoxScope.() -> Unit) {
    QNAppThemeInternal {
        NormalDensityScale {
            content()
        }
    }
}

@Composable
fun QNDialogTheme(appScreenshotState: ScreenshotState, content: @Composable () -> Unit) {
    QNAppThemeInternal {
        // Dialog和Page的LocalProvider是独立的，在dialog内部无法获取到Page的LocalProvider
        // 所以讲page的ScreenshotState传递过来再塞给Dialog
        CompositionLocalProvider(LocalScreenshot provides appScreenshotState) {
            content()
        }
    }
}

@Composable
private fun QNAppThemeInternal(content: @Composable BoxScope.() -> Unit) {

    var uiMode by remember { mutableStateOf(DefaultUiMode) }

    DisposableEffect(Unit) {
        // 监听宿主日夜间切换
        val onThemeChanged: (isDark: Boolean) -> Unit = { isDark ->
            uiMode = uiMode.copy(isDarkTheme = isDark)
        }
        AppUiModeRegistry.registerOnThemeChanged(onThemeChanged)

        // 监听宿主字号缩放
        val onTextChanged: (textScaleRatio: Double) -> Unit = { textScaleRatio ->
            uiMode = uiMode.copy(textScaleLevel = appStatus().currentTextScaleGradient())
        }
        AppUiModeRegistry.registerOnTextScaleRatioChanged(onTextChanged)

        // 监听宿主字体变化
        val onFontChanged: (font: String) -> Unit = { font ->
            @OptIn(InternalResourceApi::class)
            uiMode =
                uiMode.copy(textFontFamily = if (font.isNullOrEmpty()) null else FontResource(font))
        }
        AppUiModeRegistry.registerOnFontFamilyChanged(onFontChanged)

        // 解注册，防止内存泄漏
        onDispose {
            AppUiModeRegistry.unregisterOnThemeChanged(onThemeChanged)
            AppUiModeRegistry.unregisterOnTextScaleRatioChanged(onTextChanged)
            AppUiModeRegistry.unregisterOnFontFamilyChanged(onFontChanged)
        }
    }

    CompositionLocalProvider(LocalUiMode provides uiMode) {
        ComposeViewLog.warn("QNAppTheme") { "current ui mode: ${uiMode}" }
        val darkTheme = isAppInDarkTheme()
        val (colorScheme, backgroundTheme, drawableTheme) = appTheme(darkTheme)

        val pageViewWidth = pageViewWidth()
        val pageViewHeight = pageViewHeight()
        CompositionLocalProvider(
            LocalBackgroundTheme provides backgroundTheme,
            LocalColorScheme provides colorScheme,
            LocalDrawable provides drawableTheme,
        ) {
            Box(modifier = Modifier.size(pageViewWidth, pageViewHeight)) {
                content()
            }
        }
    }
}

/**
 * 强制使用light主题
 */
@Composable
fun ForceLightTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalBackgroundTheme provides LightBackgroundTheme,
        LocalColorScheme provides LightColorScheme,
        LocalDrawable provides LightDrawableTheme,
        content = content
    )
}

/**
 * 强制使用dark主题
 */
@Composable
fun ForceDarkTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalBackgroundTheme provides DarkBackgroundTheme,
        LocalColorScheme provides DarkColorScheme,
        LocalDrawable provides DarkDrawableTheme,
        content = content
    )
}

typealias OnThemeChanged = (isDark: Boolean) -> Unit
typealias OnTextScaleRatioChanged = (textScaleRatio: Double) -> Unit
typealias OnFontFamilyChanged = (font: String) -> Unit

object AppUiModeRegistry {

    // 监听宿主日夜间切换
    private val onThemeChangedListeners = mutableListOf<OnThemeChanged>()

    // 监听宿主字号缩放
    val onTextChangedListeners = mutableListOf<OnTextScaleRatioChanged>()

    // 监听宿主字体变化
    val onFontChangedListeners = mutableListOf<OnFontFamilyChanged>()

    init {
        appStatus().subscribeTheme { isDark ->
            onThemeChangedListeners.forEach { it.invoke(isDark) }
        }

        appStatus().subscribeTextScaleRatio { textScaleRatio ->
            onTextChangedListeners.forEach { it.invoke(textScaleRatio) }
        }

        appStatus().subscribeFontFamily { fontFamily ->
            onFontChangedListeners.forEach { it.invoke(fontFamily) }
        }
    }

    internal fun registerOnThemeChanged(listener: OnThemeChanged) {
        onThemeChangedListeners.add(listener)
    }

    internal fun unregisterOnThemeChanged(listener: OnThemeChanged) {
        onThemeChangedListeners.remove(listener)
    }

    internal fun registerOnTextScaleRatioChanged(listener: OnTextScaleRatioChanged) {
        onTextChangedListeners.add(listener)
    }

    internal fun unregisterOnTextScaleRatioChanged(listener: OnTextScaleRatioChanged) {
        onTextChangedListeners.remove(listener)
    }

    internal fun registerOnFontFamilyChanged(listener: OnFontFamilyChanged) {
        onFontChangedListeners.add(listener)
    }

    internal fun unregisterOnFontFamilyChanged(listener: OnFontFamilyChanged) {
        onFontChangedListeners.remove(listener)
    }
}

@Composable
private fun appTheme(darkTheme: Boolean): Triple<ColorScheme, BackgroundTheme, DrawableTheme> {

    val colorSpace = colorScheme(darkTheme)
    val backgroundTheme = if (darkTheme) DarkBackgroundTheme else LightBackgroundTheme
    val drawableTheme = if (darkTheme) DarkDrawableTheme else LightDrawableTheme

    return Triple(colorSpace, backgroundTheme, drawableTheme)
}

@Composable
private fun colorScheme(darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
}