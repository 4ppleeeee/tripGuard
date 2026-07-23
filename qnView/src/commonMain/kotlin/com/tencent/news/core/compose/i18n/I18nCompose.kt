package com.tencent.news.core.compose.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.news.core.platform.api.appI18n
import com.tencent.news.core.platform.i18n.UiText
import com.tencent.news.core.platform.i18n.resolve

/**
 * 语言变化注册中心，类似 [com.tencent.news.core.compose.scaffold.theme.AppUiModeRegistry]。
 *
 * 采用懒订阅：当有首个监听者注册时才向 [appI18n] 订阅语言变化，
 * 确保能拿到宿主注入的真实 [IAppI18n] 实例。
 */
object I18nRegistry {

    private val onLanguageChangedListeners = mutableListOf<(languageTag: String) -> Unit>()
    private var subscribed = false

    private fun ensureSubscribed() {
        if (subscribed) return
        subscribed = true
        appI18n().subscribeLanguageChanged { languageTag ->
            onLanguageChangedListeners.forEach { it.invoke(languageTag) }
        }
    }

    internal fun register(listener: (languageTag: String) -> Unit) {
        ensureSubscribed()
        onLanguageChangedListeners.add(listener)
    }

    internal fun unregister(listener: (languageTag: String) -> Unit) {
        onLanguageChangedListeners.remove(listener)
    }
}

/**
 * 将 [UiText] 解析为最终字符串，并在语言切换时自动触发 Compose 重组。
 *
 * 用法：
 * ```
 * QnText(text = qString(StringKey.COMMON_CANCEL.res()))
 * ```
 *
 * 性能说明：在 [uiText] 与当前语言不变时，会复用上一次的解析结果，避免在高频
 * 重组路径（如随滚动变化的 sticky 区域）上反复触发 Native 资源查找与 Context 创建。
 */
@Composable
fun qString(uiText: UiText): String {
    var languageVersion by remember { mutableStateOf(appI18n().currentLanguageTag()) }

    DisposableEffect(Unit) {
        val onLanguageChanged: (String) -> Unit = { newTag ->
            languageVersion = newTag
        }
        I18nRegistry.register(onLanguageChanged)
        onDispose {
            I18nRegistry.unregister(onLanguageChanged)
        }
    }

    // 仅在 uiText（按 data class 结构相等性比较）或语言切换时重新解析，
    // 重组热路径上直接命中缓存。
    return remember(uiText, languageVersion) { uiText.resolve() }
}
