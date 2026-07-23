package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.tencent.kuikly.compose.animation.core.FloatTweenSpec
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.gestures.animateScrollBy
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.lazy.LazyColumn
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.foundation.lazy.rememberLazyListState
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.Dp
import kotlinx.coroutines.delay


enum class AutoCarouselDirection {
    Vertical,
    Horizontal
}

// 根据数组大小设置缓冲系数
private val Collection<*>.autoCarouselBufferFactor: Int
    get() = 2   // 目前测着，新版kuikly不需要这么大buffer，轮播效果还可以

@Composable
fun <T> AutoCarouselView(
    items: List<T>,
    itemSize: Dp,   // 元素的高度（Vertical模式） 或 宽度（Horizontal模式）
    direction: AutoCarouselDirection = AutoCarouselDirection.Vertical,
    interval: Long = 2000,
    startDelay: Long = 2000,
    initialIndex: Int = 0,  // 新增参数
    onItemClick: ((item: T) -> Unit)? = null,
    onItemSelected: ((index: Int) -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    require(items.isNotEmpty()) { "Carousel items cannot be empty" }

    // 检查 initialIndex 是否在合法范围内
    val safeInitialIndex = if (items.isEmpty()) 0 else initialIndex.coerceIn(0, items.size - 1)


    // 计算虚拟列表总长度（原始长度 * 缓冲系数）
    val virtualCount = items.size * items.autoCarouselBufferFactor

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = safeInitialIndex
    )

    // 处理自动轮播滚动
    LaunchedEffect(items) {
        if (items.size <= 1) return@LaunchedEffect
        // 滚动到 safeInitialIndex
        var currentIndex = safeInitialIndex
        state.scrollToItem(currentIndex)
        onItemSelected?.invoke(currentIndex % items.size)

        // 延迟 startDelay 后开始滚动
        delay(startDelay)

        while (true) {
            // 接近缓存区末尾时，滚动到初始位置附近，继续轮播
            if (currentIndex >= virtualCount - 1) {
                currentIndex = safeInitialIndex + currentIndex % items.size
                state.scrollToItem(currentIndex)
            }
            currentIndex = (currentIndex + 1) % virtualCount

            val scrollDistance = when (direction) {
                AutoCarouselDirection.Vertical -> state.layoutInfo.viewportSize.height
                AutoCarouselDirection.Horizontal -> state.layoutInfo.viewportSize.width
            }
            state.animateScrollBy(scrollDistance.toFloat(), FloatTweenSpec(400))

            onItemSelected?.invoke(currentIndex % items.size)

            delay(interval)
        }
    }

    when (direction) {
        AutoCarouselDirection.Vertical ->
            Box(Modifier.fillMaxWidth().height(itemSize)) {
                InfiniteLazyColumn(
                    state = state,
                    items = items,
                    itemSize = itemSize,
                    onItemClick = onItemClick,
                    content = content
                )
            }

        AutoCarouselDirection.Horizontal ->
            Box(Modifier.fillMaxHeight().width(itemSize)) {
                InfiniteLazyRow(
                    state = state,
                    items = items,
                    itemSize = itemSize,
                    onItemClick = onItemClick,
                    content = content
                )
            }
    }
}

@Composable
private fun <T> InfiniteLazyColumn(
    state: LazyListState,
    items: List<T>,
    itemSize: Dp,
    onItemClick: ((T) -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    LazyColumn(
        state = state,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxWidth().height(itemSize),
    ) {
        items(
            count = items.size * items.autoCarouselBufferFactor,
            contentType = { virtualIndex -> virtualIndex.mod(items.size) }
        ) { virtualIndex ->
            Box(
                modifier = Modifier.fillMaxWidth().height(itemSize)
                    .apply {
                        if (onItemClick != null) {
                            clickable { onItemClick(items[virtualIndex.mod(items.size)]) }
                        }
                    }
            ) {
                content(items[virtualIndex.mod(items.size)])
            }
        }
    }
}

@Composable
private fun <T> InfiniteLazyRow(
    state: LazyListState,
    items: List<T>,
    itemSize: Dp,
    onItemClick: ((T) -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    LazyRow(
        state = state,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            count = items.size * items.autoCarouselBufferFactor,
            contentType = { virtualIndex -> virtualIndex.mod(items.size) }
        ) { virtualIndex ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(itemSize)
                    .apply {
                        if (onItemClick != null) {
                            clickable { onItemClick(items[virtualIndex.mod(items.size)]) }
                        }
                    }
            ) {
                content(items[virtualIndex.mod(items.size)])
            }
        }
    }
}