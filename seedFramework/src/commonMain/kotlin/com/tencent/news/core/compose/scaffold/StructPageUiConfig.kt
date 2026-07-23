package com.tencent.news.core.compose.scaffold

import com.tencent.news.core.extension.KColor
import kotlinx.coroutines.flow.MutableStateFlow

data class StructPageUiConfig(
    // 是否需要控制状态栏，主要用于头部视频场景
    val statusBarChangeSwitch: MutableStateFlow<Boolean> = MutableStateFlow(false),
)

data class PullRefreshHeaderUiState(
    val refreshingBgColor: Int = PULL_REFRESH_REFRESHING_BG_LIGHT,
    val refreshingTextColor: Int = PULL_REFRESH_REFRESHING_TEXT_LIGHT,
    val refreshedBgColor: Int = PULL_REFRESH_REFRESHED_BG,
    val refreshedTextColor: Int = PULL_REFRESH_REFRESHED_TEXT_LIGHT,
    val lottieUrl: String = "",
) {
    companion object {
        fun default(isDarkTheme: Boolean): PullRefreshHeaderUiState =
            PullRefreshHeaderUiState(
                refreshingBgColor = if (isDarkTheme) {
                    PULL_REFRESH_REFRESHING_BG_DARK
                } else {
                    PULL_REFRESH_REFRESHING_BG_LIGHT
                },
                refreshingTextColor = if (isDarkTheme) {
                    PULL_REFRESH_REFRESHING_TEXT_DARK
                } else {
                    PULL_REFRESH_REFRESHING_TEXT_LIGHT
                },
                refreshedBgColor = PULL_REFRESH_REFRESHED_BG,
                refreshedTextColor = if (isDarkTheme) {
                    PULL_REFRESH_REFRESHED_TEXT_DARK
                } else {
                    PULL_REFRESH_REFRESHED_TEXT_LIGHT
                }
            )
    }
}

data class PullRefreshResultUiState(
    val shouldShowResult: Boolean = false,
    val text: String = "",
)

private val PULL_REFRESH_REFRESHING_BG_LIGHT = KColor.toColorInt("#FFFFFFFF")
private val PULL_REFRESH_REFRESHING_BG_DARK = KColor.toColorInt("#FF1F1F1F")
private val PULL_REFRESH_REFRESHING_TEXT_LIGHT = KColor.toColorInt("#FF999999")
private val PULL_REFRESH_REFRESHING_TEXT_DARK = KColor.toColorInt("#FF696969")
private val PULL_REFRESH_REFRESHED_BG = KColor.toColorInt("#FF3377FF")
private val PULL_REFRESH_REFRESHED_TEXT_LIGHT = KColor.toColorInt("#FFFFFFFF")
private val PULL_REFRESH_REFRESHED_TEXT_DARK = KColor.toColorInt("#FFE6E6E6")
