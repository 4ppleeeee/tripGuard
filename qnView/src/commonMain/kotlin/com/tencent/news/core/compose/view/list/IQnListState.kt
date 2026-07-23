package com.tencent.news.core.compose.view.list

import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.foundation.gestures.ScrollableState
import com.tencent.kuikly.compose.foundation.gestures.scrollBy
import com.tencent.kuikly.compose.foundation.lazy.LazyListState
import com.tencent.kuikly.compose.foundation.lazy.grid.LazyGridState
import com.tencent.kuikly.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import com.tencent.kuikly.compose_dsl.kuikly.extension.contentOffset

/**
 * 列表状态抽象接口，用于统一管理不同类型列表组件的状态
 * 支持 LazyColumn 和 LazyVerticalGrid 等组件的状态管理
 */
@Stable
interface IQnListState {
    val realListState: ScrollableState          // compose用的真正state对象

    val layoutInfo: IQnLayoutInfo               // 布局信息
    val firstVisibleItemIndex: Int              // 第一个可见项的索引
    val firstVisibleItemScrollOffset: Int       // 第一个可见项的滚动偏移量

    val contentOffset: Int                      // 内容偏移量
    val isScrollInProgress: Boolean             // 是否正在滚动
    val canScrollForward: Boolean               // 是否还可以往前滚动

    suspend fun scrollToItem(index: Int, offset: Int = 0)   // 滚动到指定索引位置
    suspend fun animateScrollToItem(index: Int) // 带动画地滚动到指定索引位置
    suspend fun stopFling()                     // 停止惯性滚动（目前不好使，待中台支持）
    suspend fun scrollBy(delta: Float)          // 滚动指定距离
}

/**
 * 布局信息抽象接口
 */
@Stable
interface IQnLayoutInfo {
    val totalItemsCount: Int                        // item总数
    val visibleItemsInfo: List<IQnVisibleItemInfo>  // 当前可见的item信息列表
    val firstItemOffset: Int // 其实可以从visibleItemsInfo读取，这个优化性能，避免频繁创建对象触发GC
    val viewportEndOffset: Int
}

/**
 * 可见项信息抽象接口
 */
@Stable
interface IQnVisibleItemInfo {
    val index: Int      // item索引
    val size: Int       // item大小
    val offset: Int     // item偏移量
}

/**
 * LazyListState 适配器
 */
@Stable
internal class LazyListStateAdapter(private val lazyListState: LazyListState) : IQnListState {
    override val realListState: LazyListState
        get() = lazyListState

    override val layoutInfo: IQnLayoutInfo = object : IQnLayoutInfo {
        override val totalItemsCount: Int
            get() = lazyListState.layoutInfo.totalItemsCount

        override val visibleItemsInfo: List<IQnVisibleItemInfo>
            get() = lazyListState.layoutInfo.visibleItemsInfo.map { info ->
                object : IQnVisibleItemInfo {
                    override val index: Int = info.index
                    override val size: Int = info.size
                    override val offset: Int = info.offset
                }
            }

        override val firstItemOffset: Int
            get() = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0

        override val viewportEndOffset: Int
            get() = lazyListState.layoutInfo.viewportEndOffset
    }

    override val firstVisibleItemIndex: Int
        get() = lazyListState.firstVisibleItemIndex

    override val firstVisibleItemScrollOffset: Int
        get() = lazyListState.firstVisibleItemScrollOffset

    override val contentOffset: Int
        get() = lazyListState.contentOffset

    override val isScrollInProgress: Boolean
        get() = lazyListState.isScrollInProgress

    override val canScrollForward: Boolean
        get() = lazyListState.canScrollForward

    override suspend fun scrollToItem(index: Int, offset: Int) {
        lazyListState.scrollToItem(index, offset)
    }

    override suspend fun animateScrollToItem(index: Int) {
        lazyListState.animateScrollToItem(index)
    }

    override suspend fun stopFling() {
        lazyListState.scrollBy(0f)
    }

    override suspend fun scrollBy(delta: Float) {
        lazyListState.scrollBy(delta)
    }
}

/**
 * LazyGridState 适配器
 */
@Stable
internal class LazyGridStateAdapter(private val lazyGridState: LazyGridState) : IQnListState {
    override val realListState: LazyGridState
        get() = lazyGridState

    override val layoutInfo: IQnLayoutInfo = object : IQnLayoutInfo {
        override val totalItemsCount: Int
            get() = lazyGridState.layoutInfo.totalItemsCount

        override val visibleItemsInfo: List<IQnVisibleItemInfo>
            get() = lazyGridState.layoutInfo.visibleItemsInfo.map { info ->
                object : IQnVisibleItemInfo {
                    override val index: Int = info.index
                    override val size: Int = info.size.height
                    override val offset: Int = info.offset.y
                }
            }

        override val firstItemOffset: Int
            get() = lazyGridState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset?.y ?: 0

        override val viewportEndOffset: Int
            get() = lazyGridState.layoutInfo.viewportEndOffset
    }

    override val firstVisibleItemIndex: Int
        get() = lazyGridState.firstVisibleItemIndex

    override val firstVisibleItemScrollOffset: Int
        get() = lazyGridState.firstVisibleItemScrollOffset

    // todo dev 目前只有游戏使用grid且用不上此属性，等后续使用时再实现
    override val contentOffset: Int
        get() = 0

    override val isScrollInProgress: Boolean
        get() = lazyGridState.isScrollInProgress

    override val canScrollForward: Boolean
        get() = lazyGridState.canScrollForward

    override suspend fun scrollToItem(index: Int, offset: Int) {
        lazyGridState.scrollToItem(index, offset)
    }

    override suspend fun animateScrollToItem(index: Int) {
        lazyGridState.animateScrollToItem(index)
    }

    override suspend fun stopFling() {
        lazyGridState.scrollBy(0f)
    }

    override suspend fun scrollBy(delta: Float) {
        lazyGridState.scrollBy(delta)
    }
}

/**
 * LazyStaggeredGridState 适配器 - 瀑布流网格
 */
@Stable
internal class LazyStaggeredGridStateAdapter(private val lazyStaggeredGridState: LazyStaggeredGridState) :
    IQnListState {
    override val realListState: LazyStaggeredGridState
        get() = lazyStaggeredGridState

    override val layoutInfo: IQnLayoutInfo = object : IQnLayoutInfo {
        override val totalItemsCount: Int
            get() = lazyStaggeredGridState.layoutInfo.totalItemsCount

        override val visibleItemsInfo: List<IQnVisibleItemInfo>
            get() = lazyStaggeredGridState.layoutInfo.visibleItemsInfo.map { info ->
                object : IQnVisibleItemInfo {
                    override val index: Int = info.index
                    override val size: Int = info.size.height
                    override val offset: Int = info.offset.y
                }
            }

        override val firstItemOffset: Int
            get() = lazyStaggeredGridState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset?.y ?: 0

        override val viewportEndOffset: Int
            get() = lazyStaggeredGridState.layoutInfo.viewportEndOffset
    }

    override val firstVisibleItemIndex: Int
        get() = lazyStaggeredGridState.firstVisibleItemIndex

    override val firstVisibleItemScrollOffset: Int
        get() = lazyStaggeredGridState.firstVisibleItemScrollOffset

    override val contentOffset: Int
        get() = 0 // todo: 同LazyGridState

    override val isScrollInProgress: Boolean
        get() = lazyStaggeredGridState.isScrollInProgress

    override val canScrollForward: Boolean
        get() = lazyStaggeredGridState.canScrollForward

    override suspend fun scrollToItem(index: Int, offset: Int) {
        lazyStaggeredGridState.scrollToItem(index, offset)
    }

    override suspend fun animateScrollToItem(index: Int) {
        lazyStaggeredGridState.animateScrollToItem(index)
    }

    override suspend fun stopFling() {
        lazyStaggeredGridState.scrollBy(0f)
    }

    override suspend fun scrollBy(delta: Float) {
        lazyStaggeredGridState.scrollBy(delta)
    }

}

/**
 * PagerState 适配器 - 垂直分页滚动
 */
@Stable
class PagerStateAdapter(private val pagerState: com.tencent.kuikly.compose.foundation.pager.PagerState) :
    IQnListState {
    override val realListState: ScrollableState
        get() = pagerState

    override val layoutInfo: IQnLayoutInfo = object : IQnLayoutInfo {
        override val totalItemsCount: Int
            get() = pagerState.pageCount

        override val visibleItemsInfo: List<IQnVisibleItemInfo>
            get() = listOf(object : IQnVisibleItemInfo {
                override val index: Int = pagerState.currentPage
                override val size: Int = 0 // Pager不提供此信息
                override val offset: Int = 0
            })
        override val firstItemOffset: Int
            get() = 0

        override val viewportEndOffset: Int
            get() = 0 // Pager不提供此信息
    }

    override val firstVisibleItemIndex: Int
        get() = pagerState.currentPage

    override val firstVisibleItemScrollOffset: Int
        get() = 0 // Pager使用页面为单位，不提供像素级偏移

    override val contentOffset: Int
        get() = 0

    override val isScrollInProgress: Boolean
        get() = pagerState.isScrollInProgress

    override val canScrollForward: Boolean
        get() = pagerState.canScrollForward

    override suspend fun scrollToItem(index: Int, offset: Int) {
        pagerState.scrollToPage(index)
    }

    override suspend fun animateScrollToItem(index: Int) {
        pagerState.animateScrollToPage(index)
    }

    override suspend fun stopFling() {
        pagerState.scrollBy(0f)
    }

    override suspend fun scrollBy(delta: Float) {
        pagerState.scrollBy(delta)
    }
}