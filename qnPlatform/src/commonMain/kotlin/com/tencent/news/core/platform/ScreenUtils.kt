package com.tencent.news.core.platform

import com.tencent.news.core.isIOSPlatform

/**
 * 屏幕相关工具类
 */
object ScreenUtils {
    /**
     * 获取标题栏高度值
     * iOS: 44
     * 其他平台: 49
     * @return Int 标题栏高度的数值
     */
    fun getTitleBarHeight(): Int {
        return if (isIOSPlatform()) {
            44
        } else {
            49
        }
    }

    /**
     * pad 宽屏内容 适配规则
     * @param 实际宽度
     * @param default 需要减去view已有的padding
     * @return 左右padding值
     */
    fun getPadContentPadding(width: Int, default: Int = 0): Int {
        val padding = when {
            width < 640 -> default
            width < 834 -> 26
            width < 1024 -> 56
            width >= 1024 -> 200
            else -> 0
        } - default
        return if (padding > 0) padding else 0
    }
}