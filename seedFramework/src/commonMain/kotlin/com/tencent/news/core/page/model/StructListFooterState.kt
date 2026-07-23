package com.tencent.news.core.page.model

enum class StructListFooterState {
    WAITING_FOR_MORE,   // 待机状态，可以触发加载更多
    LOADING,            // 加载中，成功后切回 WAITING_FOR_MORE
    ERROR,              // 加载出错，需点击重试
    NO_MORE             // 没有更多数据了
}