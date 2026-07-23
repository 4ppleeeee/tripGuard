package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.ListWidget
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.platform.api.INetworkBuilder


interface IFeedsDataProcessor {

    // 开始构建网络请求的最早时机：如果想插入特殊参数，可以用 requestEnv.extraRequestParams
    // 【注意】这里如果强行添加重名参数，会覆盖公参
    fun onBeforeRequest(requestEnv: FeedsRequestEnv, request: DataRequest) {}

    fun onAfterNetworkBuilderCreated(requestEnv: FeedsRequestEnv, builder: INetworkBuilder<*>) {}

    // 列表数据加工开始（和 onAfterProcess 配对）
    fun onBeforeProcess(requestEnv: FeedsRequestEnv, newPageWidget: StructPageWidget) {}

    // 构建item列表（时序介于 onBeforeProcess 和 onAfterProcess 之间）
    // 结论里已经包含了广告插入
    fun onBuildItemListResult(
        requestEnv: FeedsRequestEnv,
        allData: MutableList<IKmmFeedsItem>,
        newData: MutableList<IKmmFeedsItem>,
    ) {
    }

    // 列表数据加工完（和 onBeforeProcess 配对）
    fun onAfterProcess(
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget,
        feedsResult: FeedsProcessResult,
    ) {
    }

    // 专门处理 ListWidget 的回调（时序介于 onBeforeProcess 和 onAfterProcess 之间）
    // 想给列表强插一些cell啥的，一般用这个
    fun onPreProcessListWidget(requestEnv: FeedsRequestEnv, listWidget: ListWidget) {}

    // 注意，这个和 onPreProcessListWidget 的区别：这个只有新一刷里下发的 listWidget 才回调
    // onPreProcessListWidget 是页面存在的每一个 listWidget 都回调
    fun onPreProcessNewListWidget(requestEnv: FeedsRequestEnv, newListWidget: ListWidget) {}

    // 用于插入模块分区的头尾
    fun onCreateSectionHeaders(index: Int, listWidget: ListWidget): List<IKmmFeedsItem>? = null
    fun onCreateSectionFooters(index: Int, listWidget: ListWidget): List<IKmmFeedsItem>? = null

    // 整个数据加工流程最后一步：成功
    fun onProcessSucceed(
        requestEnv: FeedsRequestEnv,
        newPageWidget: StructPageWidget,
        feedsResult: FeedsProcessResult,
    ) {
    }

    // 整个数据加工流程最后一步：失败
    fun onProcessError(requestEnv: FeedsRequestEnv, result: ResultEx) {}

}

data class FeedsProcessResult(
    val allData: List<IKmmFeedsItem>,
    val newData: List<IKmmFeedsItem>,
)