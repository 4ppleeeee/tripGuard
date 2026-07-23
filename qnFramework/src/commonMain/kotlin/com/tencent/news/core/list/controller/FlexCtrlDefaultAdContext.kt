package com.tencent.news.core.list.controller

import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.tads.api.IAdFeedsContext
import com.tencent.news.core.tads.api.IFeedDataProvider
import com.tencent.news.core.tads.feeds.AdFeedsRequest
import com.tencent.news.core.tads.model.AdScene

internal open class FlexCtrlDefaultAdContext(val flexCtrl: FlexCtrl) : IAdFeedsContext {

    override fun getArticleItem(): IKmmFeedsItem? = flexCtrl.pageItem?.invoke()

    override fun getFeedDataProvider(): IFeedDataProvider? = flexCtrl

    override fun findExtraItem(idStr: String): IKmmFeedsItem? =
        flexCtrl.getExtraList().find { it.baseDto.idStr == idStr }

    override fun buildAdRequestEnv(adScene: AdScene, adRequest: AdFeedsRequest) {
        super.buildAdRequestEnv(adScene, adRequest)
    }

}