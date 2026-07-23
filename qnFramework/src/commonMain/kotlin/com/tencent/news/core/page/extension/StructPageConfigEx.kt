package com.tencent.news.core.page.extension

import com.tencent.news.core.page.model.StatusBarColorMode
import com.tencent.news.core.page.model.StructPageConfig
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.page.model.StructWidget


fun StructPageConfig.isStatusBarLightBgDarkIcon(collapsePercent: Float): Boolean {
    val collapseSlop = 0.95f // Header滑动超过95%，认为是折叠态

    // true-白背景黑图标； false-黑背景白图标
    return when (defaultStatusBarColorMode) {
        StatusBarColorMode.ALWAYS_DARK_ICON -> true
        StatusBarColorMode.ALWAYS_LIGHT_ICON -> false

        // 下面两种模式，随着Header折叠状态进行反向变化
        StatusBarColorMode.LIGHT_BG_DARK_ICON -> (collapsePercent < collapseSlop)
        StatusBarColorMode.DARK_BG_LIGHT_ICON -> !(collapsePercent < collapseSlop)
    }
}

fun StructWidget?.findStructPageConfig(): StructPageConfig? =
    (this?.findStructPageWidget() as? StructPageWidget2)?.pageConfig