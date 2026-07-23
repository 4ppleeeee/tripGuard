package com.tencent.news.core.compose.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.wrapContentWidth
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.platform.pageViewHeight
import com.tencent.news.core.compose.platform.pageViewWidth
import com.tencent.news.core.compose.scaffold.modifiers.width
import com.tencent.news.core.platform.WindowSizeClass
import com.tencent.news.core.platform.WindowSizeClassifier

/** 当前弹窗的展示类型 CompositionLocal，由弹窗根节点注入 */
val LocalAdaptiveDialogType = staticCompositionLocalOf<AdaptiveDialog.DisplayType?> { null }

/**
 * 弹窗自适应配置对象。
 *
 * 定义弹窗在不同窗口断点下的展示策略（底部弹出 / 居中 / 自定义），
 * 以及对应的宽度模式和圆角行为。
 *
 * ## 架构定位
 * - Compose 侧通过 [LocalAdaptiveDialogType] + [windowStyle] 获取当前断点下的样式
 * - Android View 侧通过 `AdaptiveDialogEx` 中的 DSL 使用 [DisplayType.select] 获取 [WindowStyle]
 *
 * ## 使用示例（Compose）
 * ```kotlin
 * CompositionLocalProvider(LocalAdaptiveDialogType provides AdaptiveDialog.DisplayType.BottomSheet) {
 *     Box(
 *         contentAlignment = Alignment.BottomCenter.adaptDialogPosition(),
 *         modifier = Modifier.adaptDialogWidth { fillMaxWidth() }
 *     ) {
 *         DialogContent(shape = RoundedCornerShape(16.dp).adaptDialogCorner())
 *     }
 * }
 * ```
 *
 * Author: joejhzhou
 * Date: 2026/4/11
 */
object AdaptiveDialog {

    /**
     * 弹窗多尺寸展示类型。
     *
     * 为每个窗口断点（COMPACT / MEDIUM / EXPANDED）分别指定一个 [WindowStyle]，
     * 运行时通过 [select] 方法根据当前断点获取对应的样式。
     *
     * ## 预置类型
     * - [Center]：所有断点下居中，宽度自适应
     * - [CenterFixed]：所有断点下居中，宽度固定 375dp
     * - [BottomSheet]：小屏底部弹出，大屏居中固定 375dp
     * - [BottomSheetLarge]：小屏和中屏底部弹出，超大屏（Pad）居中固定 600dp
     * - [Custom]：自定义各断点的样式
     */
    sealed class DisplayType(
        val compatStyle: WindowStyle,
        val mediumStyle: WindowStyle,
        val expandStyle: WindowStyle
    ) {

        /**
         * 各类屏幕下都居中
         */
        object Center : DisplayType(
            compatStyle = WindowStyle.Center,
            mediumStyle = WindowStyle.Center,
            expandStyle = WindowStyle.Center
        )

        /**
         * 各类屏幕下都居中, 同时限制宽度为375
         */
        object CenterFixed : DisplayType(
            compatStyle = WindowStyle.CenterFixed(375),
            mediumStyle = WindowStyle.CenterFixed(375),
            expandStyle = WindowStyle.CenterFixed(375)
        )

        /**
         * 小屏下底部，大屏下居中
         */
        object BottomSheet : DisplayType(
            compatStyle = WindowStyle.BottomSheet,
            mediumStyle = WindowStyle.CenterFixed(375),
            expandStyle = WindowStyle.CenterFixed(375)
        )

        /**
         * 小屏幕和大屏幕下底部，超大（pad）下居中
         */
        object BottomSheetLarge : DisplayType(
            compatStyle = WindowStyle.BottomSheet,
            mediumStyle = WindowStyle.BottomSheet,
            expandStyle = WindowStyle.CenterFixed(600)
        )

        /**
         * 自定义
         */
        class Custom(compatStyle: WindowStyle, mediumStyle: WindowStyle, expandStyle: WindowStyle) :
            DisplayType(compatStyle, mediumStyle, expandStyle)

        /**
         * 根据当前窗口断点选择对应的 [WindowStyle]。
         *
         * @param mode 当前窗口断点分类
         * @return 该断点下应使用的弹窗样式
         */
        fun select(mode: WindowSizeClass): WindowStyle = when (mode) {
            WindowSizeClass.COMPACT -> compatStyle
            WindowSizeClass.MEDIUM, WindowSizeClass.MEDIUM_PORTRAIT -> mediumStyle
            WindowSizeClass.EXPANDED, WindowSizeClass.EXPANDED_PORTRAIT -> expandStyle
        }
    }

    /**
     * 弹窗宽度模式。
     *
     * - [MATCH_PARENT]：铺满父容器（底部弹出时）
     * - [WRAP_CONTENT]：自适应内容宽度（居中自适应时）
     * - [FIXED]：固定宽度，取 [WindowStyle.maxWidthDp] 值
     */
    enum class WidthMode {
        MATCH_PARENT,
        WRAP_CONTENT,
        FIXED,
    }

    /**
     * 弹窗在特定断点下的显示样式。
     *
     * 描述弹窗的位置（底部/居中）和宽度行为（铺满/自适应/固定值）。
     *
     * @property isBottom 是否为底部弹出样式
     * @property isCenter 是否为居中样式
     * @property widthMode 宽度计算模式
     * @property maxWidthDp 当 [widthMode] 为 [WidthMode.FIXED] 时的固定宽度（dp）
     */
    sealed class WindowStyle(
        val isBottom: Boolean,
        val isCenter: Boolean,
        val widthMode: WidthMode,
        val maxWidthDp: Int = -1
    ) {
        object Center : WindowStyle(
            isBottom = false,
            isCenter = true,
            widthMode = WidthMode.WRAP_CONTENT
        )

        class CenterFixed(maxWidthDp: Int) : WindowStyle(
            isBottom = false,
            isCenter = true,
            widthMode = WidthMode.FIXED,
            maxWidthDp = maxWidthDp
        )

        object BottomSheet : WindowStyle(
            isBottom = true,
            isCenter = false,
            widthMode = WidthMode.MATCH_PARENT
        )
    }

    @Composable
    fun windowStyle(): WindowStyle? {
        val type = LocalAdaptiveDialogType.current ?: return null
        val widthDp = pageViewWidth().value.toInt()
        val heightDp = pageViewHeight().value.toInt()
        return remember(type, widthDp, heightDp) {
            type.select(WindowSizeClassifier.classify(widthDp, heightDp))
        }
    }

    /**
     * 基于[WindowStyle]适配弹窗位置:底部/居中
     */
    @Composable
    fun Alignment.adaptDialogPosition(): Alignment {
        val windowStyle = windowStyle() ?: return this
        if (windowStyle.isBottom) {
            return Alignment.BottomCenter
        }
        if (windowStyle.isCenter) {
            return Alignment.Center
        }
        return this
    }

    /**
     * 基于[WindowStyle]适配弹窗宽度：铺满/375/600/自适应
     */
    @Composable
    fun Modifier.adaptDialogWidth(fallback: Modifier.() -> Modifier): Modifier {
        val style = windowStyle() ?: return fallback()
        if (style.widthMode == WidthMode.MATCH_PARENT) {
            return this.fillMaxWidth()
        }
        if (style.widthMode == WidthMode.WRAP_CONTENT) {
            return this.wrapContentWidth()
        }
        return this.width(style.maxWidthDp.toFloat())
    }

    /**
     * 基于[WindowStyle]适配弹窗圆角：居中的时候需要4周圆角
     */
    @Composable
    fun RoundedCornerShape.adaptDialogCorner(): RoundedCornerShape {
        val style = windowStyle() ?: return this
        return if (style.isCenter) {
            RoundedCornerShape(topStart)
        } else {
            this
        }
    }
}