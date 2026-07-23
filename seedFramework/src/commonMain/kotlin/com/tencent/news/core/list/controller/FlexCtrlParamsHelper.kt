package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.isNotNullOrBlank
import com.tencent.news.core.extension.noneNullMap
import com.tencent.news.core.extension.safePutAll
import com.tencent.news.core.list.api.IContextDtoHolder
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.dataRepo
import com.tencent.news.core.list.api.IListRefreshData
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.constants.isCloudReplaceAction
import com.tencent.news.core.list.extension.bindArticlePage
import com.tencent.news.core.list.extension.bindCloudRerankArticlePage
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.tads.constants.AdRefreshTypeEx
import com.tencent.news.core.tads.feeds.AdFeedsRequest
import com.tencent.news.qnchannel.api.getEntityParams
import com.tencent.news.qnchannel.api.getRequestDomain
import com.tencent.news.qnchannel.api.getRequestIp

internal class FlexCtrlParamsHelper(val flexCtrl: FlexCtrl) {

    private var currentRequestPage = 0
    internal val listPagingRecorder = ListPagingRecorder()

    fun createAdFeedsRequest(requestEnv: FeedsRequestEnv) = AdFeedsRequest(
        refreshType = AdRefreshTypeEx.mapFrom(requestEnv.dataEnv.refreshForward),
        brushNum = currentRequestPage
    ).apply {
        env.resetByTime = requestEnv.dataEnv.resetByTime
        if (requestEnv.dataEnv.refreshAction.isCloudReplaceAction()) {
            skipAdList = true
        }
    }

    fun resetPagingParams() {
        currentRequestPage = 0
        listPagingRecorder.clear()
    }

    fun increasePagingParams(newResult: Any?) {
        currentRequestPage++
        if (newResult is IListRefreshData) {
            listPagingRecorder.record(newResult)
        }
    }

    fun markPageToEnv(requestEnv: FeedsRequestEnv) {
        requestEnv.dataEnv.updatePageNum(currentRequestPage)
    }

    fun bindArticlePage(target: List<IContextDtoHolder?>?) {
        bindArticlePage(target, currentRequestPage)
    }

    fun bindCloudRerankArticlePage(target: List<IContextDtoHolder?>?) {
        bindCloudRerankArticlePage(target, currentRequestPage - 1)
    }

    // 信息流请求公参（含广告）
    fun addCommonNetworkParams(
        dataRequest: DataRequest,
        adFeedsRequest: AdFeedsRequest,
        networkBuilder: NetworkBuilder<*>,
        requestEnv: FeedsRequestEnv,
    ) {
        val dataEnv = requestEnv.dataEnv

        val commonParams = mutableMapOf<String, String>()
        commonParams.safePutAll(requestEnv.commonParams)                // 业务宿主注入的参数
        commonParams.safePutAll(buildListRequestParams(requestEnv))     // 【重要】列表通用公参
        commonParams.safePutAll(buildPagingRequestParams(requestEnv))   // 【重要】列表分页公参
        commonParams.safePutAll(buildAdRequestParams(adFeedsRequest))   // 【重要】广告-请求参数

        val finalParams = mutableMapOf<String, Any>()
            .safePutAll(commonParams)                   // 先加公参
            .safePutAll(networkBuilder.params)          // repo自定义的参数
            .safePutAll(requestEnv.extraRequestParams)  // 特殊业务额外添加参数
            .safePutAll(dataRequest.reqdata)            // 后台下发的参数（优先级最高，可覆盖前面的）
        networkBuilder.params = finalParams

        // 【重要】这几个参数，老接口里要求url里也要放一份（尤其是 chlid），这个兼容逻辑要保留
        val forceUrlParamsKey = listOf("chlid", "page", "forward")
        val commonForceUrlParams = forceUrlParamsKey
            .associateWith { key -> commonParams[key] }
            .noneNullMap()

        // 如果 dataRepo 禁用了 forceUrlParams，则不注入默认的 chlid/page/forward 到 URL 上
        val disableForceUrlParams = flexCtrl.dataRepo().disableForceUrlParams()

        val finalForceUrlParams = if (disableForceUrlParams) {
            networkBuilder.forceUrlParams?.toMutableMap() ?: mutableMapOf()
        } else {
            mutableMapOf<String, String>()
                .safePutAll(commonForceUrlParams)           // 先加公参
                .safePutAll(networkBuilder.forceUrlParams)  // repo自定义的参数
        }

        networkBuilder.forceUrlParams = finalForceUrlParams

        if (isDebug()) {
            val channelInfo = dataEnv.channelInfo
            val requestDomain = channelInfo.getRequestDomain()
            val requestIp = channelInfo.getRequestIp()

            if (requestDomain.isNotNullOrBlank() || requestIp.isNotNullOrBlank()) {
                networkBuilder.headers = mapOf(
                    "Request-Domain" to requestDomain,
                    "Request-Ip" to requestIp,
                )
                NewsChannelLog.debug { "请求测试环境 Header：${networkBuilder.headers}" }
            }
        }
    }

    // kmm内部：列表通用公参
    private fun buildListRequestParams(requestEnv: FeedsRequestEnv): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // 【重要】刷新方式
        result["forward"] = requestEnv.dataEnv.refreshForward.toString()

        // 频道信息公参
        requestEnv.dataEnv.channelInfo.apply {
            // 非频道场景下，chlid主要用于上报，使用当前页面所在的二级频道
            result["chlid"] = channelKey

            // 频道的分类字段，可以作为扩展使用
            // 【特殊业务】拉取竖版视频频道无限时，这个参数有用（标识的是竖版视频所在的二级频道），接入层有处理逻辑
            if (env.channelType.isNotEmpty()) {
                result["channelType"] = env.channelType
            }

            // 频道相关透传字段，可用于接入层区分频道数据（例如：tag_id可以放在channel_entity_id中）
            result.putAll(getEntityParams())

            // 可用于区分频道ui样式
            result["channelShowType"] = channelShowType.toString()
        }

        return result
    }

    // kmm内部：列表分页公参
    private fun buildPagingRequestParams(requestEnv: FeedsRequestEnv): Map<String, String> {
        // 注意：首屏时要清空，算法侧会使用page=0做特殊判断或上报
        val resetParams = (requestEnv.dataEnv.refreshForward == ListRefreshForward.RESET)

        val result = mutableMapOf<String, String>()
        if (isPreviewDebugging()) {
            return result // 预览接口这几个参数会有影响，先去掉
        }

        if (resetParams) {
            requestEnv.dataEnv.pageNum = 0
            result["page"] = "0" // 【重要】page参数会被推荐侧使用，page=0有特殊的首屏推荐逻辑
            result["list_transparam"] = ""
            result["last_time"] = ""
        } else {
            requestEnv.dataEnv.updatePageNum(currentRequestPage)
            result["page"] = currentRequestPage.toString() // 【重要】page参数会被推荐侧使用，page=0有特殊的首屏推荐逻辑
            result["list_transparam"] = listPagingRecorder.listTransParam
            result["last_time"] = listPagingRecorder.timestamp.toString()
        }
        return result
    }

    // 广告请求参数
    private fun buildAdRequestParams(adFeedsRequest: AdFeedsRequest): Map<String, String>? {
        val adCtrl = flexCtrl.getAdCtrl() ?: return null
        return adCtrl.createAdReqData(adFeedsRequest).toRequestParams()
    }

    private fun isPreviewDebugging(): Boolean {
        return false // 预览先不做
//        return getPageJumpInfo()?.optString("draftId").isNotNullOrBlank()
    }

}
