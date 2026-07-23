package com.tencent.news.core.compose.scaffold.skin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.tencent.news.core.compose.scaffold.registry.LocalPageSkin
import com.tencent.news.core.compose.scaffold.theme.AppUiModeRegistry
import com.tencent.news.core.compose.scaffold.theme.OnThemeChanged
import com.tencent.news.core.compose.scaffold.theme.isAppInDarkTheme
import com.tencent.news.core.page.model.StructPageTheme

/**
 * 专题皮肤主题访问对象
 */
object StructPageSkinTheme {

    val skinColor: PageSkin?
        @Composable
        @ReadOnlyComposable
        get() = LocalPageSkin.current.value
}

/**
 * 自动处理日夜间模式切换和主题变化
 */
@Composable
fun rememberStructPageSkinColor(
    structPageTheme: StructPageTheme?
): MutableState<PageSkin?> {
    val isDarkTheme = isAppInDarkTheme()

    val currentTheme = remember(structPageTheme, isDarkTheme) {
        if (structPageTheme == null) {
            null
        } else if (isDarkTheme) {
            structPageTheme.mapToNightTheme()
        } else {
            structPageTheme
        }
    }

    val skinColorState = remember(structPageTheme, isDarkTheme) {
        mutableStateOf(
            currentTheme?.toStructPageSkinColor(isDarkTheme)
        )
    }

    val currentStructPageTheme = rememberUpdatedState(structPageTheme)
    val currentSkinColorState = rememberUpdatedState(skinColorState)

    DisposableEffect(Unit) {
        val onThemeChanged: OnThemeChanged = { isDark ->
            val theme = if (currentStructPageTheme.value == null) {
                null
            } else if (isDark) {
                currentStructPageTheme.value?.mapToNightTheme()
            } else {
                currentStructPageTheme.value
            }
            currentSkinColorState.value.value = theme?.toStructPageSkinColor(isDark)
        }
        AppUiModeRegistry.registerOnThemeChanged(onThemeChanged)

        onDispose {
            AppUiModeRegistry.unregisterOnThemeChanged(onThemeChanged)
        }
    }
    return skinColorState
}

/**
 * 监听日夜间模式切换，触发回调
 * 
 * 参考 rememberStructPageSkinColor 的实现，使用 rememberUpdatedState 确保回调中总是使用最新的引用
 * 
 * @param onThemeChanged 主题切换回调，参数为是否为夜间模式
 */
@Composable
fun rememberThemeChangeListener(
    onThemeChanged: (Boolean) -> Unit
) {
    // 使用 rememberUpdatedState 确保回调中总是使用最新的 onThemeChanged 引用
    val currentCallback = rememberUpdatedState(onThemeChanged)
    
    DisposableEffect(Unit) {
        val callback: OnThemeChanged = { isDark ->
            // 调用最新的回调
            currentCallback.value.invoke(isDark)
        }
        
        // 注册主题监听器
        AppUiModeRegistry.registerOnThemeChanged(callback)
        
        // 页面销毁时自动解注册
        onDispose {
            AppUiModeRegistry.unregisterOnThemeChanged(callback)
        }
    }
}