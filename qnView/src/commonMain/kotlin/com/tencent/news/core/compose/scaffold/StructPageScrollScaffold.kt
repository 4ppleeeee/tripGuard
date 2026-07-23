package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.news.core.compose.view.list.IQnListState
import kotlinx.coroutines.delay

@Stable
data class StructPageScrollScaffold constructor(
    val rootListState: IQnListState,                    // 页面最外层整体的listState
    val selectedListState: MutableState<IQnListState?>, // 当前选中子tab的listState
    val listHeight: Dp,
    val pageHeight: Dp,
) {
    private val mainListState get() = selectedListState.value

    suspend fun scrollToIndex(
        index: Int,
        animated: Boolean = false,
        scrollRootForIndex: Boolean = false,
        delayTime: Long = 0,
    ) {
        if (delayTime > 0) {
            delay(delayTime)
        }
        if (index == 0) {
            // 【大坑】一定要先滑动子view，否则在iOS/鸿蒙上，由于联合滚动view是锁定机制，
            // mainList有偏移量时，rootList处于锁定状态无法滑动，整个页面无法置顶
            // （安卓联合滚动机制不同，所以没问题）
            if (animated) {
                mainListState?.animateScrollToItem(0)
                rootListState.animateScrollToItem(0)
            } else {
                mainListState?.scrollToItem(0)
                rootListState.scrollToItem(0)
            }
        } else {
            if (scrollRootForIndex && index > 0) {
                val lastIndex = rootListState.layoutInfo.totalItemsCount - 1
                if (animated) {
                    rootListState.animateScrollToItem(lastIndex)
                } else {
                    rootListState.scrollToItem(lastIndex)
                }
            }

            if (animated) {
                mainListState?.animateScrollToItem(index)
            } else {
                mainListState?.scrollToItem(index)
            }
        }
    }

    suspend fun scrollToTop(animated: Boolean = false) {
        scrollToIndex(0, animated)
    }

    suspend fun scrollToBottom(animated: Boolean = false) {
        val listState = mainListState ?: return
        val lastIndex = listState.layoutInfo.totalItemsCount - 1
        scrollToIndex(lastIndex, animated, scrollRootForIndex = true)
    }

    suspend fun collapseHeader(animated: Boolean = false) {
        val lastIndex = rootListState.layoutInfo.totalItemsCount - 1
        if (animated) {
            rootListState.animateScrollToItem(lastIndex)
        } else {
            rootListState.scrollToItem(lastIndex)
        }
    }

    suspend fun fixBottomScrollState() {
        // kuikly列表bug：listState嵌套复杂时，有时scrollToIndex后底部滑动状态判断不对
        // 需要反向滑动一下才能校准
        scrollBy(1f)
    }

    suspend fun scrollBy(dy: Float) {
        mainListState?.scrollBy(dy)
    }

}
