package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.StructListHeaderState
import com.tencent.news.core.page.model.StructPageWidget


internal class ListHeaderStateProcessor(
    private val ctrl: IFlexibleFeedsController
) : IFeedsDataProcessor {

    override fun onBeforeRequest(requestEnv: FeedsRequestEnv, request: DataRequest) {
        if (requestEnv.getRefreshForward() == ListRefreshForward.TOP_REFRESH) {
            ctrl.headerState.value = StructListHeaderState.LOADING
        }
    }

    override fun onProcessSucceed(
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget,
        feedsResult: FeedsProcessResult
    ) {
        if (requestEnv.isPullDownRefresh()) {
            // 先设置 COMPLETE，让 UI 层的 pullToRefreshItem 收起 loading 动画
            ctrl.headerState.value = StructListHeaderState.COMPLETE
            // 再根据是否还有 top_more 决定最终状态：
            // 没有 top_more 时设为 NO_MORE，禁止继续下拉；有则回到 WAITING_FOR_MORE
            if (ctrl.rootWidget.hasTopMore()) {
                ctrl.headerState.value = StructListHeaderState.WAITING_FOR_MORE
            } else {
                ctrl.headerState.value = StructListHeaderState.NO_MORE
            }
            return
        }
        if (ctrl.rootWidget.hasTopMore()) {
            ctrl.headerState.value = StructListHeaderState.WAITING_FOR_MORE
        } else {
            ctrl.headerState.value = StructListHeaderState.NO_MORE
        }
    }

    override fun onProcessError(requestEnv: FeedsRequestEnv, result: ResultEx) {
        if (requestEnv.getRefreshForward() == ListRefreshForward.TOP_REFRESH) {
            ctrl.headerState.value = StructListHeaderState.ERROR
        }
    }

    /**
     * 判断是否是手动下拉刷新
     */
    private fun FeedsRequestEnv.isPullDownRefresh() = dataEnv.isPullDownRefresh()
}