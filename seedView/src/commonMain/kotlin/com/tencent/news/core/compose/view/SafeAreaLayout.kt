package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.core.base.EdgeInsets
import com.tencent.news.core.compose.scaffold.modifiers.height
import com.tencent.news.core.compose.scaffold.modifiers.width
import com.tencent.news.core.compose.scaffold.theme.ColorScheme
import com.tencent.news.core.compose.scaffold.theme.LocalColorScheme
import com.tencent.news.core.platform.api.ScreenOrientation
import com.tencent.news.core.platform.api.appWindow

@Composable
fun SafeAreaLayout(
    modifier: Modifier = Modifier,
    contentHandlesSafeArea: Boolean,
    safeAreaThemeOverride: ColorScheme?,
    safeAreaBackgroundColorProvider: @Composable () -> Color,
    content: @Composable () -> Unit
) {
    if (!contentHandlesSafeArea) {
        content()
        return
    }

    if (isLandscape()) {
        // 横屏：用 Row 包裹，左右放带背景色的 Spacer，只填充 content 旁边的 safe area
        LandscapeSafeAreaLayout(
            modifier = modifier,
            safeAreaThemeOverride = safeAreaThemeOverride,
            safeAreaBackgroundColorProvider = safeAreaBackgroundColorProvider,
            content = content
        )
    } else {
        PortraitSafeAreaLayout(
            modifier = modifier,
            safeAreaThemeOverride = safeAreaThemeOverride,
            safeAreaBackgroundColorProvider = safeAreaBackgroundColorProvider,
            content = content
        )
    }
}

@Composable
private fun PortraitSafeAreaLayout(
    modifier: Modifier = Modifier,
    safeAreaThemeOverride: ColorScheme?,
    safeAreaBackgroundColorProvider: @Composable () -> Color,
    content: @Composable () -> Unit
) {
    // 竖屏：底部放带背景色的 Spacer
    Column(modifier = modifier) {
        content()
        SafeAreaSpacer(
            safeAreaThemeOverride,
            safeAreaBackgroundColorProvider,
            spacer = { background: Color, safeArea: EdgeInsets ->
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(safeArea.bottom)
                        .background(background)
                )
            }
        )
    }
}

@Composable
private fun LandscapeSafeAreaLayout(
    modifier: Modifier = Modifier,
    safeAreaThemeOverride: ColorScheme?,
    safeAreaBackgroundColorProvider: @Composable () -> Color,
    content: @Composable () -> Unit
) {

    Row(modifier = modifier) {
        // 左侧 safe area 填充
        SafeAreaSpacer(
            safeAreaThemeOverride = safeAreaThemeOverride,
            safeAreaBackgroundColorProvider = safeAreaBackgroundColorProvider,
            spacer = { background: Color, safeArea: EdgeInsets ->
                Spacer(
                    modifier = Modifier
                        .width(safeArea.top)
                        .height(1.dp)
                        // .background(background)
                )
            }
        )

        // 中间 content 区域
        Column(modifier = Modifier.weight(1f)) {
            content()
        }

        // 右侧 safe area 填充
        SafeAreaSpacer(
            safeAreaThemeOverride = safeAreaThemeOverride,
            safeAreaBackgroundColorProvider = safeAreaBackgroundColorProvider,
            spacer = { background: Color, safeArea: EdgeInsets ->
                Spacer(
                    modifier = Modifier
                        .width(safeArea.bottom)
                        .height(1.dp)
                        // .background(background)
                )
            }
        )
    }
}


@Composable
fun SafeAreaSpacer(
    safeAreaThemeOverride: ColorScheme?,
    safeAreaBackgroundColorProvider: @Composable () -> Color,
    spacer: @Composable (background: Color, safeArea: EdgeInsets) -> Unit
) {
    val safeArea = LocalConfiguration.current.safeAreaInsets
    // 用 safeAreaThemeOverride 包裹，确保颜色读取到 dialog 的 theme 而非宿主页面的 ColorScheme
    if (safeAreaThemeOverride != null) {
        CompositionLocalProvider(LocalColorScheme provides safeAreaThemeOverride) {
            spacer(safeAreaBackgroundColorProvider(), safeArea)
        }
    } else {
        spacer(safeAreaBackgroundColorProvider(), safeArea)
    }
}

private fun isLandscape(): Boolean {
    return appWindow().getScreenOrientation() == ScreenOrientation.LANDSCAPE
}