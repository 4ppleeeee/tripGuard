package com.tencent.news.core.tads.api

import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.tads.feeds.AdFeedsRequest
import com.tencent.news.core.tads.model.AdScene


// 广告业务对宿主的依赖

interface IAdFeedsContext {
    fun getExchangeMajorLoid(): Int? = null

    // 当前页面Item
    fun getArticleItem(): IKmmFeedsItem?

    // 创建请求时回调，用于添加额外请求参数：
    fun buildAdRequestEnv(adScene: AdScene, adRequest: AdFeedsRequest) {}

    // 通过id查找 extra_list中的数据
    fun findExtraItem(idStr: String): IKmmFeedsItem? = null
    fun bindFeedDataProvider(provider: IFeedDataProvider) {}
    fun getFeedDataProvider(): IFeedDataProvider? = null
}

interface IFeedDataProvider {
    fun getAllList(): List<IKmmFeedsItem>   // 全量列表数据
    fun getExtraList(): List<IKmmFeedsItem> = emptyList()   // 额外列表数据
}