package com.tencent.news.core.compose.scaffold

// 竖版pager生命周期（比如：竖版视频、电台页 等等）
interface IStructVerticalPagerLifecycle {

    /**
     * 更新当前可见的索引
     *
     * @param index 当前可见的索引位置
     */
    fun updateCurrentVisibleIndex(index: Int)

    /**
     * 当用户在最后一条数据时尝试继续向上滑动时调用
     * 用于展示"已经是最后一篇"等提示
     */
    fun onBoundaryScrollAttempt()

    /**
     * 当用户在第一条数据时尝试继续向下滑动时调用
     * 默认不处理，由特定业务页面按需覆写。
     */
    fun onTopBoundaryScrollAttempt() = Unit

}
