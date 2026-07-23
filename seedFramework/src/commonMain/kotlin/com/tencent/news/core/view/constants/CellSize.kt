package com.tencent.news.core.view.constants

import com.tencent.news.core.isIOSPlatform
import kotlin.math.min

data class CellSize(
    val aspectRatio: Float = 0f,
    val maxWidthInDp: Float = if (isIOSPlatform()) 430f else 375f,
    val initHeightInDp: Float = 0f,
    val enableScale: Boolean = false,    // dp是否响应字体变化
    val enableDynamicHeight: Boolean = false    // 是否接收宿主侧实测高度回传刷新 Cell 高度
) {
    // 内容区宽度（大屏时，内容居中摆放，左右留边距）
    fun adaptContentWidth(cellWidthInDp: Float): Float =
        min(maxWidthInDp, cellWidthInDp)

    fun adaptCellHeight(cellWidthInDp: Float): Float =
        adaptContentWidth(cellWidthInDp) / aspectRatio

    // 大屏适配算法：
    // - 常规竖屏时：内容以 aspectRatio 计算为准
    // - 大屏时：重新计算 ratio 限制高度，保证内容区尺寸不超过 adaptMaxWidthInDp（避免出现一个大图cell撑满全屏）
    fun adaptAspectRatio(cellWidthInDp: Float): Float {
        val adaptCellHeight = adaptCellHeight(cellWidthInDp)
            .takeIf { it > 0 }
            ?: return aspectRatio
        return cellWidthInDp / adaptCellHeight
    }
}