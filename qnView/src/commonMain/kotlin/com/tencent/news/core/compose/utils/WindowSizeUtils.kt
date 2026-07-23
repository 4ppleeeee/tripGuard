package com.tencent.news.core.compose.utils

import androidx.compose.runtime.Composable
import com.tencent.news.core.compose.platform.pageViewWidth
import com.tencent.news.core.platform.api.getShiplyInt

object WindowSizeUtils {
    @Composable
    fun isBigScreen(): Boolean = pageViewWidth().value > getShiplyInt(
        "like_pad_window_width_threshold",
        600
    )

    @Composable
    fun isSuperBigScreen(): Boolean = pageViewWidth().value > getShiplyInt(
            "large_pad_window_width_threshold",
            960
        )

    @Composable
    fun getWindowSizeType(): WindowSizeType = when {
        isSuperBigScreen() -> WindowSizeType.SUPER_BIG
        isBigScreen() -> WindowSizeType.BIG
        else -> WindowSizeType.NORMAL
    }
}

enum class WindowSizeType {
    NORMAL,
    BIG,
    SUPER_BIG
}