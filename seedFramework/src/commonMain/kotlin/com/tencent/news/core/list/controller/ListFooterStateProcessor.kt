package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.StructListFooterState
import com.tencent.news.core.page.model.StructPageWidget

internal class ListFooterStateProcessor(
    private val ctrl: IFlexibleFeedsController
) : IFeedsDataProcessor {

    override fun onBeforeRequest(requestEnv: FeedsRequestEnv, request: DataRequest) {
        if (requestEnv.needRefreshFooter()) {
            if (requestEnv.getRefreshForward() == ListRefreshForward.BOTTOM_REFRESH) {
                ctrl.footerState.value = StructListFooterState.LOADING
            }
        }
    }

    override fun onProcessSucceed(
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget,
        feedsResult: FeedsProcessResult
    ) {
        if (requestEnv.shouldUpdateFooter()) {
            ctrl.footerState.value = if (ctrl.rootWidget.hasMore()) {
                StructListFooterState.WAITING_FOR_MORE
            } else {
                StructListFooterState.NO_MORE
            }
        }
    }

    override fun onProcessError(requestEnv: FeedsRequestEnv, result: ResultEx) {
        if (requestEnv.needRefreshFooter()) {
            if (requestEnv.getRefreshForward() == ListRefreshForward.BOTTOM_REFRESH) {
                ctrl.footerState.value = StructListFooterState.ERROR
            }
        }
    }

    private fun FeedsRequestEnv.needRefreshFooter(): Boolean =
        getRefreshAction() != ListRefreshAction.QUERY_EXPANSION

    private fun FeedsRequestEnv.shouldUpdateFooter(): Boolean =
        needRefreshFooter() &&
                !(getRefreshForward() == ListRefreshForward.TOP_REFRESH && getRefreshAction() == ListRefreshAction.AUTO_MORE)

}