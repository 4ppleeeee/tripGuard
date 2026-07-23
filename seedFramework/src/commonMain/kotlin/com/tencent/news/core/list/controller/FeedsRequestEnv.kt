package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.getCurTimestampMillis
import com.tencent.news.core.list.api.ListRefreshReason
import com.tencent.news.core.list.api.StructDataEnv
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.constants.isCloudReplaceAction
import com.tencent.news.core.platform.api.INetworkBuilder
import com.tencent.news.core.platform.api.INetworkResponse
import com.tencent.news.core.platform.getCurTimeMillis


data class FeedsRequestEnv constructor(
    val dataEnv: StructDataEnv,
    val commonParams: Map<String, String>? = null,
    val processor: IFeedsDataProcessor,
) {

    val requestStartTime: Long = getCurTimeMillis() // 时间点：开始发起请求
    var requestCreatedTime: Long = 0                // 时间点：请求构建完毕
    var networkResponseTime: Long = 0               // 时间点：收到网络回调
    var parserStartTime: Long = 0                   // 时间点：开始解析时间
    var parserCost: Long = 0                        // 耗时：json解析（会包含在网络回调耗时里）
    var buildItemListCost: Long = 0                 // 耗时：构建item列表（含排重、广告插入、item加工）
    var processCallbackCost: Long = 0               // 耗时：宿主业务回调派发
    var url: String = ""                            // 请求url

    // 如果想要获取宿主的原始 request 和 response，通过这俩接口对象进行转换
    // （这俩对象的实现类由宿主提供的，宿主可以拿着这俩对象进行强转）

    // 本次请求链路的 builder，创建后会赋值到这里（通过builder可以获取 INetworkRequest）
    var networkBuilder: INetworkBuilder<*>? = null

    // 本次请求链路的返回结果，收到回调后赋值到这里
    var networkResponse: INetworkResponse<*>? = null

    // 本次请求中额外添加的参数（优先级最高，能覆盖其他所有请求参数，包括公参）
    val extraRequestParams: MutableMap<String, Any> by lazy { mutableMapOf() }

    fun getRefreshForward(): ListRefreshForward = dataEnv.refreshForward

    fun getRefreshAction(): ListRefreshAction = dataEnv.refreshAction

    fun logStr4SLO(): String {
        return "请求构建：${requestCreatedTime - requestStartTime}ms, " +
                "解析：${parserCost}ms, " +
                "网络(含解析)：${networkResponseTime - requestCreatedTime}ms, " +
                "构建item列表：${buildItemListCost}ms, " +
                "回调派发：${processCallbackCost}ms, " +
                "请求链路总耗时=${getCurTimestampMillis() - requestStartTime}ms"
    }

    fun getListRefreshReason(): ListRefreshReason {
        if (getRefreshAction().isCloudReplaceAction()) {
            return ListRefreshReason.CLOUD_REPLACE
        }
        if (getRefreshAction() == ListRefreshAction.QUERY_EXPANSION) {
            return ListRefreshReason.EXPAND
        }
        return when (getRefreshForward()) {
            ListRefreshForward.RESET -> ListRefreshReason.RESET
            ListRefreshForward.TOP_REFRESH -> ListRefreshReason.TOP_REFRESH
            ListRefreshForward.BOTTOM_REFRESH -> ListRefreshReason.BOTTOM_REFRESH
        }
    }

}