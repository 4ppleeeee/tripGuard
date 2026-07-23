package com.tencent.news.core.compose.view.list


/**
 * 检查是否滚动到底部
 */
internal fun IQnListState.isScrolledToBottom(threshold: Int): Boolean {
    val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
    val totalItems = layoutInfo.totalItemsCount
    return lastVisibleIndex >= totalItems - threshold && totalItems > 0
}

/**
 * 检查是否滚动到顶部
 */
internal fun IQnListState.isScrolledToTop(threshold: Int): Boolean {
    val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1
    return firstVisibleIndex <= threshold && firstVisibleIndex >= 0
}

internal suspend fun IQnListState.scrollToBottom(animate: Boolean = false) {
    if (layoutInfo.totalItemsCount > 0) {
        val targetIndex = layoutInfo.totalItemsCount - 1
        if (animate) {
            animateScrollToItem(targetIndex)
        } else {
            scrollToItem(targetIndex)
        }
    }
}