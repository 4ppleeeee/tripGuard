package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.safeAddAll
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.ListWidget
import com.tencent.news.core.page.model.NewsListWidget
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.platform.api.INetworkBuilder

// processor支持多个，这里暂时不需要优先级控制，有需要后续再设计
internal class FeedsDataProcessorReDispatcher(
    private val targets: List<IFeedsDataProcessor>,
) : IFeedsDataProcessor {

    override fun onBeforeRequest(requestEnv: FeedsRequestEnv, request: DataRequest) {
        dispatch { it.onBeforeRequest(requestEnv, request) }
    }

    override fun onAfterNetworkBuilderCreated(
        requestEnv: FeedsRequestEnv,
        builder: INetworkBuilder<*>,
    ) {
        dispatch { it.onAfterNetworkBuilderCreated(requestEnv, builder) }
    }

    override fun onBeforeProcess(requestEnv: FeedsRequestEnv, newPageWidget: StructPageWidget) {
        dispatch { it.onBeforeProcess(requestEnv, newPageWidget) }
    }

    override fun onBuildItemListResult(
        requestEnv: FeedsRequestEnv,
        allData: MutableList<IKmmFeedsItem>,
        newData: MutableList<IKmmFeedsItem>,
    ) {
        dispatch { it.onBuildItemListResult(requestEnv, allData, newData) }
    }

    override fun onAfterProcess(
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget,
        feedsResult: FeedsProcessResult,
    ) {
        dispatch { it.onAfterProcess(requestEnv, newPageWidget, feedsResult) }
    }

    override fun onPreProcessListWidget(requestEnv: FeedsRequestEnv, listWidget: ListWidget) {
        dispatch { it.onPreProcessListWidget(requestEnv, listWidget) }
    }

    override fun onPreProcessNewListWidget(requestEnv: FeedsRequestEnv, newListWidget: ListWidget) {
        dispatch { it.onPreProcessNewListWidget(requestEnv, newListWidget) }
    }

    override fun onCreateSectionHeaders(
        index: Int,
        listWidget: NewsListWidget,
    ): List<IKmmFeedsItem> {
        val result = mutableListOf<IKmmFeedsItem>()
        targets.forEach {
            result.safeAddAll(it.onCreateSectionHeaders(index, listWidget))
        }
        return result
    }

    override fun onCreateSectionFooters(
        index: Int,
        listWidget: NewsListWidget,
    ): List<IKmmFeedsItem> {
        val result = mutableListOf<IKmmFeedsItem>()
        targets.forEach {
            result.safeAddAll(it.onCreateSectionFooters(index, listWidget))
        }
        return result
    }

    override fun onProcessSucceed(
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget,
        feedsResult: FeedsProcessResult,
    ) {
        dispatch { it.onProcessSucceed(requestEnv, newPageWidget, feedsResult) }
    }

    override fun onProcessError(requestEnv: FeedsRequestEnv, result: ResultEx) {
        dispatch { it.onProcessError(requestEnv, result) }
    }

    private fun dispatch(action: (IFeedsDataProcessor) -> Unit) {
        targets.forEach(action)
    }

}