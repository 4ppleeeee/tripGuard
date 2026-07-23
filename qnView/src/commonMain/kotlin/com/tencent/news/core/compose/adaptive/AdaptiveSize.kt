package com.tencent.news.core.compose.adaptive

import com.tencent.news.core.platform.WindowInfo
import com.tencent.news.core.platform.WindowSizeClass

/**
 * 自适应尺寸接口。
 *
 * 根据当前 [WindowInfo]（包含窗口尺寸和断点分类）计算出一个 dp 值。
 * 返回值 > 0 表示有效尺寸，-1 表示不限制（[NoLimit]）。
 *
 * ## 设计意图
 * 替代原来的 `Padding` / `WidthLimit` 固定类，支持更灵活的尺寸计算逻辑——
 * 不仅可以按断点返回固定值，还能基于窗口实际宽高动态计算。
 *
 * ## 使用方式
 * - 直接使用 [FixedSize]：固定值
 * - 使用 [NoLimit]：不限制（返回 -1）
 * - 使用 [adaptiveSize] DSL：为各断点分别指定 [AdaptiveSize]
 *
 * Author: joejhzhou
 * Date: 2026/6/5
 */
interface AdaptiveSize {
    /**
     * 根据窗口信息计算尺寸值。
     *
     * @param info 当前窗口信息
     * @return 尺寸值（dp），-1 表示不限制
     */
    operator fun invoke(info: WindowInfo): Int
}

/**
 * 固定尺寸实现，无论窗口状态如何始终返回 [size]。
 *
 * @param size 固定的 dp 值，-1 表示不限制
 */
class FixedSize(val size: Int) : AdaptiveSize {
    override fun invoke(info: WindowInfo): Int = size
}

/** 不限制尺寸的常量，始终返回 -1 */
object NoLimit : AdaptiveSize by FixedSize(-1)

/**
 * 按断点分配尺寸的 DSL Scope。
 *
 * 通过 [adaptiveSize] 函数创建，可为 COMPACT / MEDIUM / EXPANDED 各断点
 * 分别指定一个 [AdaptiveSize] 策略。
 *
 * ## 示例
 * ```kotlin
 * val myPadding = adaptiveSize {
 *     compactSize = NoLimit                           // COMPACT 不限宽
 *     mediumSize = 40.fixed()                        // MEDIUM 左右 40dp
 *     expandedSize = expandedPaddingSize             // EXPANDED 使用全局配置值
 * }
 * ```
 */
class AdaptiveSizeScope : AdaptiveSize {
    /** COMPACT 断点下的尺寸策略，默认不限制 */
    var compactSize: AdaptiveSize = NoLimit

    /** MEDIUM 断点下的尺寸策略，默认不限制 */
    var mediumSize: AdaptiveSize = NoLimit

    /** EXPANDED 断点下的尺寸策略，默认不限制 */
    var expandedSize: AdaptiveSize = NoLimit

    /** 将 Int 转为 [FixedSize] 的便捷扩展 */
    fun Int.fixed() = FixedSize(this)

    override fun invoke(info: WindowInfo): Int = when (info.type) {
        WindowSizeClass.COMPACT -> compactSize(info)
        WindowSizeClass.MEDIUM, WindowSizeClass.MEDIUM_PORTRAIT -> mediumSize(info)
        WindowSizeClass.EXPANDED, WindowSizeClass.EXPANDED_PORTRAIT -> expandedSize(info)
    }

}

/**
 * 创建一个按断点分配尺寸的 [AdaptiveSize]。
 *
 * @param block 配置各断点的尺寸策略
 * @return 配置完成的 [AdaptiveSize] 实例
 */
fun adaptiveSize(block: AdaptiveSizeScope.() -> Unit = {}): AdaptiveSize =
    AdaptiveSizeScope().apply(block)