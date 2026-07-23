package com.tencent.news.core.page.model

interface IStructPageWidgetCacheConfig {
    val expiredTime: Long       // 缓存过期时间，根据自己业务需求配置
    var lastUpdateTime: Long    // 上次数据刷新时间

    fun onRefreshCache() {}

    fun onHitCache() {}

    fun onNotHitCache() {}
}