package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.compose.scaffold.registry.LocalStructPageViewModel
import com.tencent.news.core.compose.scaffold.theme.DarkColorScheme
import com.tencent.news.core.compose.scaffold.theme.LightColorScheme
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.scaffold.theme.QnSkin
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
import com.tencent.news.core.platform.api.appStatus
import com.tencent.news.core.platform.api.statusBarController
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent

/**
 * 标题栏控制变量
 */
data class TitleBarTheme(
    val isHeaderCollapsed: Boolean,     // 是否折叠标题栏（影响中间btn的展示）
    val hideLeftBtns: Boolean = true,   // 是否隐藏左侧按钮

    val titleTextColor: Color,          // 标题颜色（通过这个颜色控制的标题显隐）
    val titleBarBgColor: Color,         // TitleBar整体背景色（根据 isHeaderCollapsed 会切换）
    val widgetFgColor: Color,           // TitleBar中各种按钮的颜色（根据 isHeaderCollapsed 会切换）
    val widgetBgColor: Color,           // TitleBar中各种按钮的背景色（根据 isHeaderCollapsed 会切换）
)

// 透明主题
val TransparentTheme = TitleBarTheme(
    isHeaderCollapsed = false,
    hideLeftBtns = true,
    titleTextColor = LightColorScheme.transparent,
    titleBarBgColor = LightColorScheme.transparent,
    widgetFgColor = LightColorScheme.t4,
    widgetBgColor = Color.White.changeAlpha(0.3f)
)

// Light主题
val LightTheme = TitleBarTheme(
    isHeaderCollapsed = true,
    hideLeftBtns = false,
    titleTextColor = LightColorScheme.t1,
    titleBarBgColor = LightColorScheme.bgPage,
    widgetFgColor = LightColorScheme.t1,
    widgetBgColor = LightColorScheme.bgBlock
)

// Dark主题
val DarkTheme = TitleBarTheme(
    isHeaderCollapsed = true,
    hideLeftBtns = false,
    titleTextColor = DarkColorScheme.t1,
    titleBarBgColor = DarkColorScheme.bgPage,
    widgetFgColor = DarkColorScheme.t1,
    widgetBgColor = DarkColorScheme.bgBlock
)

// 获取当前标题栏主题，方便读取各控制变量
val currentTitleBarTheme
    @Composable get() = LocalTitleBarTheme.current

// 标题栏主题的CompositionLocal
val LocalTitleBarTheme = compositionLocalOf { LightTheme }

@Composable
fun TitleBarTheme(content: @Composable () -> Unit) {
    val isDarkTheme = isAppInDarkTheme()
    val titleBarTheme = if (isDarkTheme) DarkTheme else LightTheme
    CompositionLocalProvider(LocalTitleBarTheme provides titleBarTheme, content)
}

internal interface StructTitleBarThemeProvider {
    @Composable
    fun invoke(
        isTransparent: Boolean,
        forceLight4Transparent: Boolean,
        isDarkTheme: Boolean,
    ): TitleBarTheme
}

// 根据标题栏状态获取其主题
@Composable
fun provideDefaultTitleBarTheme(
    transparentBg: Boolean,     // TitleBar背景透明
    darkTheme: Boolean,         // 黑白主题
    isIconAlwaysDark: Boolean,  // icon始终是深色
    isAlwaysShowTitle: Boolean, // 始终展示标题（常态下，标题只会在 transparentBg=false 时展示）
): TitleBarTheme {

    // 刷新StatusBar状态：
    provideDefaultStatusBarTheme(transparentBg, darkTheme)

    // 刷新TitleBar状态：
    return if (transparentBg) {
        val widgetFgColor = if (isIconAlwaysDark) {
            if (darkTheme) DarkColorScheme.backIconColor else LightColorScheme.t1
        } else {
            QnColor.t4
        }
        val titleTextColor = if (isAlwaysShowTitle) {
            if (isIconAlwaysDark) {
                if (darkTheme) DarkColorScheme.t1 else LightColorScheme.t1
            } else {
                QnColor.t4
            }
        } else {
            LightColorScheme.transparent
        }

        TransparentTheme.copy(
            widgetFgColor = widgetFgColor,
            titleTextColor = titleTextColor
        )
    } else {
        if (darkTheme) DarkTheme else LightTheme
    }
}

@Composable
private fun provideDefaultStatusBarTheme(
    transparentBg: Boolean,     // TitleBar背景透明
    darkTheme: Boolean,         // 黑白主题
) {
    fun refreshStatusBar() {
        // 直接读取最新主题，避免 LaunchedEffect 协程闭包捕获旧 darkTheme 的问题
        // （尤其是从设置页切换主题后返回时，协程不会重启，闭包里的 darkTheme 是旧值）
        val isDark = appStatus().isNightMode()
        if (transparentBg) {
            statusBarController.setWhiteBar()
        } else {
            if (isDark) {
                statusBarController.setWhiteBar()
            } else {
                statusBarController.setBlackBar()
            }
        }
    }

    val statusBarState = LocalStructPageViewModel.current
        ?.pageUiConfig?.statusBarChangeSwitch?.collectAsState()
        ?: return
    val statusBarSwitch by statusBarState

    // 重组时正常刷新：
    if (statusBarSwitch) {
        refreshStatusBar()
    }

    // 同时监听两个生命周期流，确保两种场景下都能正确恢复 StatusBar 颜色：
    // 1. LocalComposePageLifecycleFlow：Tab 切换（如从关注频道切到消息 Tab），Activity 不 pause/resume
    // 2. pageFlow：Activity 级别的 onResume（如从设置页返回）
    val tabLifecycleFlow = LocalComposePageLifecycleFlow.current
    val activityLifecycleFlow = LocalStructPageViewModel.current?.pageFlow
    LaunchedEffect(transparentBg, tabLifecycleFlow) {
        tabLifecycleFlow?.collect { event ->
            if (event == PageLifecycleEvent.ON_RESUME) {
                refreshStatusBar()
            }
        }
    }
    LaunchedEffect(transparentBg, activityLifecycleFlow) {
        activityLifecycleFlow?.collect { event ->
            if (event == PageLifecycleEvent.ON_RESUME) {
                refreshStatusBar()
            }
        }
    }
}

/**
 * 获取标题栏控件的颜色
 * 统一处理 QnSkin 和 header 折叠状态的颜色逻辑
 */
@Composable
fun getTitleBarWidgetColor(
    isHeaderCollapsed: Boolean,
    defaultColor: Color
): Color {
    return if (QnSkin?.iconColor != null) {
        if (isHeaderCollapsed) {
            QnSkin?.iconColor ?: QnColor.t4
        } else {
            QnColor.t4
        }
    } else {
        defaultColor
    }
}