package com.tencent.news.core.compose.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentWidth
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.adaptive.AdaptivePage.adaptiveCell
import com.tencent.news.core.compose.adaptive.AdaptivePage.adaptivePagePadding
import com.tencent.news.core.compose.adaptive.AdaptivePage.padding
import com.tencent.news.core.compose.adaptive.AdaptiveUiConfig.expandedPaddingSize

/**
 * 当前页面的自适应样式 CompositionLocal。
 *
 * 通过 `CompositionLocalProvider(LocalAdaptivePageStyle provides style)` 在页面根部注入，
 * 子树中的 [AdaptiveContent] 会自动读取该值决定是否限宽。
 *
 * 未提供时默认为 [AdaptivePage.Style.Normal]（不限宽）。
 */
val LocalAdaptivePageStyle = staticCompositionLocalOf<AdaptivePage.Style?> { null }

/**
 * 卡片级自适应 Box 容器。
 *
 * 在 EXPANDED 断点下将内容宽度限制为 [AdaptivePage.Style.cellWidth] 定义的固定值，
 * 防止卡片在大屏下被过度拉伸。COMPACT 下不做任何限制。
 *
 * ## 与 [AdaptiveBox] 的区别
 * - [AdaptiveBox] 使用 padding 实现居中限宽（页面级）
 * - [AdaptiveBox4Cell] 使用固定 width 限制卡片宽度（Cell 级）
 *
 * ## 示例
 * ```kotlin
 * LazyColumn {
 *     items(newsList) { item ->
 *         AdaptiveBox4Cell {
 *             NewsCard(item)  // 大屏下卡片不会被拉伸到全宽
 *         }
 *     }
 * }
 * ```
 *
 * @param modifier 附加的 Modifier，默认 fillMaxWidth
 * @param content  BoxScope 内容
 */
@Composable
fun AdaptiveBox4Cell(
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable BoxScope.() -> Unit
) {
    val style = LocalAdaptivePageStyle.current ?: AdaptivePage.Style.Normal
    Box(modifier = modifier.adaptiveCell(style)) {
        content()
    }
}

/**
 * 自适应内容容器（Compose 版）。
 *
 * 根据 [LocalAdaptivePageStyle] 和当前窗口断点决定是否限宽：
 * - 需要限宽时：以指定 dp 宽度居中显示
 * - 不限宽时：填满父容器宽度
 *
 * ## 与 [AdaptiveBox] 的区别
 * - **无 style 注入时**：`AdaptiveContent` 直接渲染 content，**不包裹任何容器**（零开销）；
 *   `AdaptiveBox` 则始终保持 Box 结构。
 * - **Scope**：本函数 content 为普通 `@Composable () -> Unit`，不提供 BoxScope；
 *   如需 `align` 等 Box 修饰符，请使用 [AdaptiveBox]。
 *
 * ## 使用场景
 * 将需要在大屏上限宽的内容（如 Header、ChannelBar、列表 item）包裹在 [AdaptiveContent] 中，
 * 而背景、分割线等全宽元素放在外部。
 *
 * ## 示例
 * ```kotlin
 * @Composable
 * fun MyHeader() {
 *     AdaptiveContent {
 *         // 此区域在 EXPANDED 下自动限宽居中
 *         Text("标题内容")
 *     }
 * }
 * ```
 *
 * @param modifier 附加的 Modifier
 * @param content  限宽区域内的内容
 *
 * @see AdaptiveBox 始终保持 Box 结构的版本，提供 BoxScope
 */
@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable () -> Unit
) {
    val style = LocalAdaptivePageStyle.current
    if (style == null) {
        content()
    } else {
        Box(modifier = modifier.adaptivePagePadding(style)) {
            content()
        }
    }
}

/**
 * 自适应 Box 容器，作为 [Box] 的 adaptive 替代品。
 *
 * 与 [AdaptiveContent] 的核心区别：
 * - **无 style 注入时**：`AdaptiveContent` 直接渲染 content 不包裹任何容器；
 *   而 `AdaptiveBox` 始终保持 Box 结构（fallback 到 [AdaptivePage.Style.Normal]，
 *   此时 modifier 不执行任何限宽操作，等价于普通 `Box(modifier)`）。
 * - **Scope**：提供 [BoxScope]，支持 `align` 等 Box 特有修饰符。
 *
 * ## 设计意图
 * 业务方可以安全地将普通 `Box` 替换为 `AdaptiveBox`，无需关心当前页面是否注入了
 * [LocalAdaptivePageStyle]——未注入时行为与普通 Box 完全一致，不会产生额外开销。
 *
 * ## 使用场景
 * 需要 Box 布局能力（如子元素对齐、叠层布局）且同时需要自适应限宽的区域。
 *
 * ## 示例
 * ```kotlin
 * @Composable
 * fun MyOverlay() {
 *     AdaptiveBox {
 *         Image(modifier = Modifier.align(Alignment.Center))
 *         Text("居中标题", modifier = Modifier.align(Alignment.BottomCenter))
 *     }
 * }
 * ```
 *
 * @param modifier 附加的 Modifier，默认 fillMaxWidth
 * @param content  BoxScope 内容
 *
 * @see AdaptiveContent 无 Box 包裹的轻量版本
 */
@Composable
fun AdaptiveBox(
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable BoxScope.() -> Unit
) {
    val style = LocalAdaptivePageStyle.current ?: AdaptivePage.Style.Normal
    Box(modifier = modifier.adaptivePagePadding(style)) {
        content()
    }
}

/**
 * 页面级自适应配置对象。
 *
 * 定义了不同断点下的限宽策略 [Style]，通过 [AdaptiveSize] 接口实现灵活的尺寸计算。
 *
 * ## 架构定位
 * - 在 Compose 侧通过 [LocalAdaptivePageStyle] 传递 Style
 * - 在 Android View 侧通过 AdaptivePageScope.pageStyle 持有 Style
 * - 两端共享 [Style.padding] / [Style.cellLimit] 计算逻辑，保证视觉一致性
 *
 * ## 预定义常量
 * - [ExpandedPadding]：EXPANDED 断点下使用 [AdaptiveUiConfig.expandedPaddingSize]
 * - [LimitCell]：MEDIUM + EXPANDED 下限制卡片宽度为 [AdaptiveUiConfig.expandedCellLimitWidth]
 */
object AdaptivePage {

    val ExpandedPadding = adaptiveSize {
        expandedSize = expandedPaddingSize
    }

    val LimitCell = adaptiveSize {
        mediumSize = AdaptiveUiConfig.expandedCellLimitWidth.fixed()
        expandedSize = AdaptiveUiConfig.expandedCellLimitWidth.fixed()
    }

    /**
     * 页面适配样式定义。
     *
     * 通过 [padding] 和 [cellLimit] 两个 [AdaptiveSize] 为各断点指定限宽策略。
     * [AdaptiveSize] 接收 [WindowInfo] 返回 dp 值，-1 表示不限宽。
     *
     * ## 预置样式
     * - [Normal]：所有断点均不限宽
     * - [SingleColumn]：EXPANDED 断点限宽 + 卡片限宽
     * - [LimitDetail]：EXPANDED 断点仅加 padding（无卡片限宽）
     * - [Custom]：自定义 padding 和 cellLimit
     *
     * ## 运行时调用
     * - `style.padding(windowInfo)` 获取当前断点下的 padding dp 值
     * - `style.cellLimit(windowInfo)` 获取当前断点下的 cell 限宽 dp 值
     */
    sealed class Style(
        val padding: AdaptiveSize = NoLimit,
        val cellLimit: AdaptiveSize = NoLimit,
    ) {

        /** 不限宽样式，所有断点下内容铺满 */
        object Normal : Style()

        /** 单列限宽样式：EXPANDED 下加 padding 限宽 + 卡片限宽 */
        object SingleColumn : Style(padding = ExpandedPadding, cellLimit = LimitCell)

        /** 详情页限宽样式：EXPANDED 下加 padding 限宽（无卡片限宽） */
        object LimitDetail : Style(padding = ExpandedPadding)

        /**
         * 自定义限宽样式。
         *
         * @param padding   各断点下的页面 padding 策略
         * @param cellLimit 各断点下的卡片宽度限制策略
         *
         * ## 示例
         * ```kotlin
         * val customStyle = AdaptivePage.Style.Custom(
         *     padding = adaptiveSize {
         *         expandedSize = 180.fixed()
         *         mediumSize = 40.fixed()
         *     },
         *     cellLimit = adaptiveSize {
         *         expandedSize = 500.fixed()
         *     }
         * )
         * ```
         */
        class Custom(padding: AdaptiveSize = NoLimit, cellLimit: AdaptiveSize = NoLimit) :
            Style(padding, cellLimit)
    }

    @Composable
    fun Style.padding() = windowInfo().let {
        remember(this, it) {
            padding(it)
        }
    }

    @Composable
    fun Style.cellWidth() = windowInfo().let {
        remember(this, it) {
            cellLimit(it)
        }
    }

    @Composable
    fun Modifier.adaptiveCell(style: Style): Modifier {
        val limitWidth = style.cellWidth()
        return if (limitWidth > 0) {
            this.wrapContentWidth(Alignment.Start).width(limitWidth.dp)
        } else {
            this
        }
    }

    @Composable
    fun Modifier.adaptivePagePadding(style: Style): Modifier {
        val padding = style.padding()
        return if (padding > 0) {
            this.padding(horizontal = padding.dp)
        } else {
            this
        }
    }
}
