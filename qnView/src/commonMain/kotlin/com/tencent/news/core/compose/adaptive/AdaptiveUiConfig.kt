package com.tencent.news.core.compose.adaptive

import com.tencent.news.core.platform.ScreenUtils
import com.tencent.news.core.platform.WindowInfo

/**
 * 自适应 UI 全局配置。
 *
 * 存储所有断点相关的默认值，供 [AdaptivePage.Style] 和各 Adapter 读取。
 * 在 Debug 模式下可通过 Shiply 远程配置动态覆盖这些值（见 `AdaptiveUi.init()`）。
 *
 * ## 配置项
 * - [expandedPagePadding]：EXPANDED 断点下页面左右固定 padding（dp）
 * - [expandedCellLimitWidth]：EXPANDED 断点下卡片最大宽度（dp）
 * - [expandedPaddingSize]：EXPANDED 断点下动态 padding 计算策略（[AdaptiveSize]），
 *   可根据实际窗口宽度动态计算，优先级高于 [expandedPagePadding]
 *
 * Author: joejhzhou
 * Date: 2026/5/11
 */
object AdaptiveUiConfig {

    /**
     * EXPANDED 断点下页面左右 padding（dp）。
     *
     * 计算公式：(目标限宽区域外留白 240dp - 基础 padding 16dp) = 224dp
     * 即内容区宽度 = 窗口宽度 - 224 * 2 = 窗口宽度 - 448dp
     */
    var expandedPagePadding = 224

    /**
     * EXPANDED 断点下卡片最大宽度（dp）。
     *
     * 计算公式：设计稿卡片宽度 340dp * 1.4 倍率 ≈ 476dp
     * 超过此宽度的卡片会被 CellCardAdaptiveAdapter 限制。
     */
    var expandedCellLimitWidth = 476

    /**
     * EXPANDED 断点下的动态 padding 计算策略。
     *
     * 作为 [AdaptiveSize] 实现，接收 [WindowInfo] 后根据实际窗口宽度
     * 通过 [ScreenUtils.getPadContentPadding] 动态计算合适的 padding 值。
     * 相比固定值 [expandedPagePadding]，能更好地适配不同尺寸的 Pad 设备。
     */
    var expandedPaddingSize: AdaptiveSize = FixedSize(expandedPagePadding)
}