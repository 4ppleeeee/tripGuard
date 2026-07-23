package com.tencent.news.core.platform

import com.tencent.news.core.platform.WindowSizeClassifier.injectClassifier

/**
 * 尺寸单位枚举。
 *
 * 用于 [IWindowSizeClassifier.classify] 方法，指明传入的宽高值的单位类型。
 * - [DP]：密度无关像素，Compose 侧直接传入
 * - [PX]：物理像素，Android View 侧通过 WindowMetrics 获取后传入
 */
enum class SizeUnit {
    DP,
    PX
}

/**
 * 窗口尺寸分类器接口。
 *
 * 定义根据窗口宽高判定断点分类的方法。
 * 宿主工程通过 [WindowSizeClassifier.injectClassifier] 注入具体实现。
 */
interface IWindowSizeClassifier {

    /**
     * 根据窗口宽高计算断点分类。
     *
     * @param width  窗口宽度，单位由 [unit] 决定
     * @param height 窗口高度，单位由 [unit] 决定
     * @param unit   宽高值的单位（[SizeUnit.DP] 或 [SizeUnit.PX]），默认 DP
     * @return 对应的 [WindowSizeClass] 枚举值
     */
    fun classify(width: Int, height: Int, unit: SizeUnit = SizeUnit.DP): WindowSizeClass
}

/**
 * 窗口尺寸分类器单例。
 *
 * 作为断点分类的统一入口，内部通过 [injectClassifier] 代理实际的分类逻辑。
 * 若未注入分类器，默认返回 [WindowSizeClass.COMPACT]。
 *
 * ## 使用方式
 * - Compose 层：通过 `AdaptivePage.Style.padding/cellLimit`、`AdaptiveContent` 等间接使用
 * - Android View 层：通过 `AdaptiveUi.computeBreakpointWithWidth()` 间接使用
 * - 也可直接调用 `WindowSizeClassifier.classify(widthDp, heightDp)`
 *
 * ## 注入时机
 * 在 Application 启动时通过 `AdaptiveUi.init()` 注入具体的断点分类器（`BreakPointWindowSizeClassifier`），
 * Debug 模式下支持通过 Shiply 远程配置动态覆盖阈值。
 *
 * ## 默认断点规则（由宿主工程注入）
 * - COMPACT：宽度 < 600dp 或 高宽比 ≥ 1.2
 * - MEDIUM：600dp ≤ 宽度 < 840dp
 * - EXPANDED：宽度 ≥ 840dp
 */
object WindowSizeClassifier : IWindowSizeClassifier {

    /** 宿主工程注入的分类器实例，为 null 时所有分类结果返回 COMPACT */
    var injectClassifier: IWindowSizeClassifier? = null

    /**
     * 根据窗口宽度（dp）判定尺寸等级
     *
     * 判定规则（统一使用 >= ）：
     * - widthDp >= expandedThreshold → EXPANDED（超大屏）
     * - widthDp >= mediumThreshold   → MEDIUM（大屏）
     * - else                         → COMPACT（普通屏）
     *
     * @param width 当前窗口宽度，单位 dp
     * @return 对应的 WindowSizeClass 枚举值
     */
    override fun classify(width: Int, height: Int, unit: SizeUnit): WindowSizeClass {
        return injectClassifier?.classify(width, height, unit) ?: WindowSizeClass.COMPACT
    }
}

/**
 * 窗口尺寸等级枚举。
 *
 * 与 Material3 WindowWidthSizeClass 概念对齐，用于在不同屏幕尺寸下驱动 UI 适配行为。
 *
 * ## 断点规则（由宿主工程 BreakPointWindowSizeClassifier 定义）
 * - **COMPACT**：窗口宽度 < 600dp 或 高宽比 ≥ 1.2（手机竖屏）
 * - **MEDIUM**：600dp ≤ 窗口宽度 < 840dp（折叠屏半展开、大手机横屏）
 * - **EXPANDED**：窗口宽度 ≥ 840dp（平板横屏、折叠屏全展开）
 *
 * ## 使用方式
 * ```kotlin
 * when (windowSizeClass) {
 *     WindowSizeClass.COMPACT -> { /* 手机布局 */ }
 *     WindowSizeClass.MEDIUM -> { /* 折叠屏布局 */ }
 *     WindowSizeClass.EXPANDED -> { /* 平板布局 */ }
 * }
 *
 * // 便捷属性
 * if (windowSizeClass.isExpanded) { /* 大屏处理 */ }
 * if (windowSizeClass.isMediumOrAbove) { /* 非手机处理 */ }
 * ```
 */
enum class WindowSizeClass(
    val medium: Boolean = false,
    val expanded: Boolean = false,
    val portrait: Boolean = false,
) {
    /** 紧凑型：手机竖屏（宽度 < 600dp 或 高宽比 ≥ 1.2） */
    COMPACT,

    MEDIUM_PORTRAIT(medium = true, portrait = true),

    /** 中等：折叠屏半展开（600dp ≤ 宽度 < 840dp） */
    MEDIUM(medium = true),

    EXPANDED_PORTRAIT(expanded = true, portrait = true),

    /** 扩展型：平板横屏/折叠屏全展开（宽度 ≥ 840dp） */
    EXPANDED(expanded = true);

    /** 是否属于大屏（MEDIUM 或 EXPANDED） */
    val isMediumOrAbove: Boolean get() = medium || expanded

    /** 是否属于超大屏 */
    val isExpanded: Boolean get() = expanded

    /** 是否属于中等屏（折叠屏半展开） */
    val isMedium: Boolean get() = medium

    /** 是否属于紧凑型 */
    val isCompact: Boolean get() = this == COMPACT

    val isPortrait: Boolean get() = portrait
}

/**
 * 窗口信息数据类，包含窗口物理尺寸和对应的断点分类。
 *
 * 作为框架内窗口变化事件的载体，由 `AdaptiveUi.computeWindowInfo()` 创建，
 * 在 [AdaptiveUi.WindowChangeListener.onWindowTypeChange] 回调中传递。
 *
 * @property windowWidthPx  窗口宽度（物理像素）
 * @property windowHeightPx 窗口高度（物理像素）
 * @property forceSizeClass     强制覆盖的断点分类；为 null 时由 [WindowSizeClassifier] 按 PX 单位自动计算
 * @property type           最终的断点分类结果（优先使用 [forceSizeClass]，否则自动计算）
 */
data class WindowInfo(
    val windowWidthPx: Int,
    val windowHeightPx: Int,
    val forceSizeClass: WindowSizeClass? = null,
    val type: WindowSizeClass = forceSizeClass ?: WindowSizeClassifier.classify(
        windowWidthPx,
        windowHeightPx,
        SizeUnit.PX
    )
)