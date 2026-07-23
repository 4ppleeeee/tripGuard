package com.tencent.news.core.app.constants

object QueryType {

    const val QUERY_READY: Int = -1               // 缓存初始化完毕，可以发起查询了
    const val QUERY_BY_EXPAND_COLLAPSE: Int = -4  // 用于cell的收起和展开，无实际意义
    const val QUERY_BY_CANCEL: Int = -5           // 查询取消

    // 注意下面这几个取值跟后台协议有关，不能随便改
    const val QUERY_BY_PULL_DOWN: Int = 0   // 顶部刷新（下拉）
    const val QUERY_BY_PULL_UP: Int = 1     // 底部刷新（上拉）
    const val QUERY_BY_RESET: Int = 2       // 首刷（reset）

    const val QUERY_BY_LAST: Int = 3        // 复用还原
}