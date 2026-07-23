package com.tencent.news.core.compose.scaffold.registry

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.NestedScrollMode
import com.tencent.kuikly.compose_dsl.kuikly.extension.nestedScroll

/**
 * 嵌套滚动配置，同时支持水平方向和垂直方向（列表）的嵌套滚动模式
 * @param scrollLeftMode 横向列表向左滑动时的嵌套滚动模式
 * @param scrollRightMode 横向列表向右滑动时的嵌套滚动模式
 * @param scrollUpMode 纵向列表向上滑动时的嵌套滚动模式
 * @param scrollDownMode 纵向列表向下滑动时的嵌套滚动模式
 */
data class NestedScrollConfig(
    val scrollLeftMode: NestedScrollMode = NestedScrollMode.SELF_FIRST,
    val scrollRightMode: NestedScrollMode = NestedScrollMode.SELF_FIRST,
    val scrollUpMode: NestedScrollMode = NestedScrollMode.SELF_FIRST,
    val scrollDownMode: NestedScrollMode = NestedScrollMode.SELF_FIRST,
)

@Composable
fun Modifier.nestedScroll(isHorizontal: Boolean): Modifier {
    val config = LocalNestedScrollConfig.current ?: return this
    return if (isHorizontal) {
        this.nestedScroll(
            scrollUp = config.scrollLeftMode,
            scrollDown = config.scrollRightMode
        )
    } else {
        this.nestedScroll(
            scrollUp = config.scrollUpMode,
            scrollDown = config.scrollDownMode
        )
    }
}