package com.tencent.news.core.list.api

import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshActionData
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.controller.IFeedsDataProcessor
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.StructListFooterState
import com.tencent.news.core.page.model.StructListHeaderState
import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.core.platform.api.INetworkBuilder
import com.tencent.news.core.tads.api.IAdFeedsController
import com.tencent.news.core.tads.api.IFeedDataProvider
import kotlinx.coroutines.flow.MutableStateFlow


interface IFlexibleFeedsController : IFeedsActionLifecycle, IFeedsItemOperator, IFeedDataProvider {

    val listPagingRecorder: IListPagingRecorder
    val footerState: MutableStateFlow<StructListFooterState>
    val headerState: MutableStateFlow<StructListHeaderState>
    /** 正在定位文章中，该状态下应忽略列表滚动和手势拖拽触发的顶部和底部自动加载 */
    val isLocatingArticle: MutableStateFlow<Boolean>
    val rootWidget: StructPageWidget2

    var listRefreshListener: IFeedsRefreshListener?

    // 触发列表刷新
    @Deprecated("用 FeedsRefreshRequest")
    fun doListRefresh(
        refreshForward: ListRefreshForward,
        refreshAction: ListRefreshAction = ListRefreshAction.NONE,
        refreshActionData: ListRefreshActionData? = null,
        commonParams: Map<String, String>? = null,
        processor: IFeedsDataProcessor?,
    ): INetworkBuilder<*>? = doListRefresh(
        request = FeedsRefreshRequest(
            refreshForward,
            refreshAction,
            refreshActionData,
            commonParams
        ),
        processor = processor
    )

    fun doListRefresh(
        request: FeedsRefreshRequest,
        processor: IFeedsDataProcessor? = null
    ): INetworkBuilder<*>?

    fun appendDefaultProcessors(vararg processors: IFeedsDataProcessor)

    fun removeDefaultProcessor(processor: IFeedsDataProcessor)

    fun doCacheRefresh(request: FeedsRefreshRequest, processor: IFeedsDataProcessor) {}

    fun getAdCtrl(): IAdFeedsController?

    // 一些配置变化可能引起广告三要素发生变化，adCtrl会重建（例如：切换‘编辑精选模式’）
    fun checkAdCtrlMayReCreate()

    fun setListLayoutType(layoutType: Int)

}

data class FeedsRefreshRequest(
    val refreshForward: ListRefreshForward,
    val refreshAction: ListRefreshAction = ListRefreshAction.NONE,
    val refreshActionData: ListRefreshActionData? = null,
    val commonParams: Map<String, String>? = null,
) {
    var resetByTime: Boolean = false

    companion object {
        fun reset() = FeedsRefreshRequest(ListRefreshForward.RESET)
        fun top() = FeedsRefreshRequest(ListRefreshForward.TOP_REFRESH)
        fun bottom() = FeedsRefreshRequest(ListRefreshForward.BOTTOM_REFRESH)
    }

}

interface IFeedsRefreshListener {

    fun onListRefresh(reason: ListRefreshReason, allData: List<IKmmFeedsItem>)

}

enum class ListRefreshReason {

    REMOVE, INSERT, REPLACE,            // 增删等基础操作api导致
    RESET, TOP_REFRESH, BOTTOM_REFRESH, // 3种基础列表刷新方式
    CACHE,                              // 查询缓存
    EXPAND,                             // 模块展开
    AD_AI_REPLACE,                      // 广告端智能
    CLOUD_REPLACE,                      // 信息流云重排

}
