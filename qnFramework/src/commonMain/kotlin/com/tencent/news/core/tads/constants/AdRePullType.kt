package com.tencent.news.core.tads.constants


annotation class AdRePullType {

    companion object {
        const val NORMAL = 0            // 普通信息流数据链路：不下发重排相关数据
        const val RE_PULL_FEEDS = 1     // 重排首次请求：也是信息流数据，但是订单会携带重排参数进行下发
        const val RE_PULL_ORDER = 2     // 重排二次请求：用于替换单篇订单
    }

}