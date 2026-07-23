package com.tencent.news.core.tads.api

import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.list.api.IFeedsActionLifecycle
import com.tencent.news.core.list.api.ListRefreshReason
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.api.INetworkRequest
import com.tencent.news.core.setup.LazyImpl
import com.tencent.news.core.tads.feeds.AdCgiParams
import com.tencent.news.core.tads.feeds.AdFeedsRequest
import com.tencent.news.core.tads.feeds.AdInsertResult
import com.tencent.news.core.tads.feeds.AdListParseResult
import com.tencent.news.core.tads.model.AdScene
import com.tencent.news.core.tads.model.IKmmAdFeedsItem


typealias AdFeedsResponse = (data: AdListParseResult?, result: ResultEx) -> Unit

/**
 * 广告controller对外接口
 */
interface IAdFeedsController : IFeedsActionLifecycle {

    var onAdAiAction: IAdFeedsAiAction?

    val adHolder: IAdHolder

    // 当前广告位 场景标识
    fun getAdScene(): AdScene

    // 拼装广告请求参数
    fun createAdReqData(request: AdFeedsRequest): AdCgiParams

    // 解析广告数据（adList字段里面的json，通常为 转义后的jsonStr格式）
    fun parseAdResponse(request: AdFeedsRequest, adListJson: String?): AdListParseResult

    /**
     * 向列表中插入广告：
     * [allData]：是待插入广告的全量列表，[insertFeedsAd]会往这个列表里插入新广告（如果列表里已有重复的广告，不会重复插入）；
     * [newData]：是当前新一刷数据，用于计算一些算法排重用的回传参数（比如 current_newslist）
     */
    fun insertFeedsAd(
        allData: MutableList<IKmmFeedsItem>,
        newData: List<IKmmFeedsItem>?,
    ): List<AdInsertResult>

    // 直接发起网络请求，从ssp拉取广告
    fun requestFromSsp(
        request: AdFeedsRequest = AdFeedsRequest(), // 请求参数：一般用默认值就够用
        onResponse: AdFeedsResponse,                 // 数据回调（也可以从adHolder取）
    ): INetworkRequest?

    // 直接发起网络请求，从广告中插服务拉取广告
    fun requestInsertContent(
        request: AdFeedsRequest = AdFeedsRequest(), // 请求参数：一般用默认值就够用
        onResponse: AdFeedsResponse,                 // 数据回调（也可以从adHolder取）
    ): INetworkRequest?

    // 直接解析ssp返回的json数据，包含adList这一层，通常为jsonObj格式（注意，同步执行要注意json耗时）
    fun parseSspAdResponse(
        request: AdFeedsRequest = AdFeedsRequest(),
        adResponseJson: String?,
    ): AdListParseResult

    // 负反馈
    fun dislikeAdItem(adItem: IKmmAdFeedsItem): Boolean

    // 列表数据发送变化时，通知给广告侧做一些额外处理
    fun onListDataChanged(reason: ListRefreshReason, data: List<IKmmFeedsItem>)

    fun onAfterInsertFeedAd(
        queryType: Int,
        allData: MutableList<IKmmFeedsItem>,
        newData: MutableList<IKmmFeedsItem>?
    ) {
    }

    fun bindCtxDtoData(allData: List<IKmmFeedsItem>, newData: List<IKmmFeedsItem>)

    fun setListLayoutType(layoutType: Int)
}

class AdFeedsContext4Item(private val item: IKmmFeedsItem?) : IAdFeedsContext {
    override fun getArticleItem(): IKmmFeedsItem? = item
}

class AdFeedsContext4Item2(private val item: () -> IKmmFeedsItem?) : IAdFeedsContext {
    override fun getArticleItem(): IKmmFeedsItem? = item()
}

// 落地页使用（注意：落地页参数可能会动态变化，例如下拉刷新后清空）：
class AdFeedsContext4Landing(
    private val item: LazyImpl<IKmmFeedsItem?>,
    private val schemeFrom: LazyImpl<String?>,
    private val pageStayTime: LazyImpl<Long?>? = null,
) : IAdFeedsContext {
    override fun getArticleItem() = item()

    override fun buildAdRequestEnv(adScene: AdScene, adRequest: AdFeedsRequest) {
        if (SchemeFrom.isFromLanding(schemeFrom())) {
            adRequest.env.isLandingPage = true
            adRequest.env.carryPreTraceId = true
            adRequest.env.stayTimeOnPage = pageStayTime?.invoke() ?: 0L
        }
    }
}
