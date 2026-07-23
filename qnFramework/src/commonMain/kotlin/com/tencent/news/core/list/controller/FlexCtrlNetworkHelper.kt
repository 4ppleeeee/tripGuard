package com.tencent.news.core.list.controller

import com.tencent.news.core.list.api.buildDefaultNetworkBuilder
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.controller.FlexCtrlCommonHelper.dataRepo
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.getCurTimeMillis

// 【重要】列表网络请求 NetworkBuilder 构建逻辑
// @see 前置 DataRequest 逻辑见：FlexCtrlRequestHelper
internal object FlexCtrlNetworkHelper {

    fun FlexCtrl.buildCommonNetworkBuilder(
        paramsHelper: FlexCtrlParamsHelper,
        requestEnv: FeedsRequestEnv,
        dataRequest: DataRequest,
    ): NetworkBuilder<*> {

        // 构建请求时，可能需要用到记录的page页码，需要提前刷新到requestEnv中
        paramsHelper.markPageToEnv(requestEnv)

        val adFeedsRequest = paramsHelper.createAdFeedsRequest(requestEnv)

        val netBuilder = innerBuildNetworkBuilder(requestEnv, dataRequest)

        // ↓↓↓ builder 基础逻辑构建完成后，进行的额外加工逻辑：

        requestEnv.url = netBuilder.url // 存一下完整请求url，目前主要是一些日志在用

        val useJsonPost4Repo = dataRepo().useJsonPost()
        if (useJsonPost4Repo != null) {
            netBuilder.useJsonPost = useJsonPost4Repo
        }

        // 【重要】构建请求公参
        paramsHelper.addCommonNetworkParams(dataRequest, adFeedsRequest, netBuilder, requestEnv)

        // todo genesisli dev 适配二级频道老参数：NewsListRequestHelper.buildCommonListRequestParams
        requestEnv.processor.onAfterNetworkBuilderCreated(requestEnv, netBuilder)

        // 如果构建request时候已经指定了parser，优先使用
        // （这种是 请求和解析 一对一，代码更内聚，后面尽量用这个）
        val originParser = netBuilder.parser

        netBuilder.updateParser { json ->
            requestEnv.parserStartTime = getCurTimeMillis()

            // 优先使用 dataRepo 进行解析（有一次拦截的机会）；
            // 未返回的话将 json 默认按照 StructPageWidget 结构解析
            val parseResult = originParser?.onParseJson(json) as? StructPageWidget
                ?: dataRepo().buildStructPageWidgetWithJson(requestEnv.dataEnv, json)
                ?: StructPageWidget().apply {
                    buildPageWithJson(json) // 这里根据json构建页面，时序要早于reBind；否则reset时会找到旧widget
                    reBindRootWidget(rootWidget) // 这里重绑root，是为了后面Processor处理时候可以find
                }

            // 【重要】广告-解析
            getAdCtrl()?.parseAdResponse(adFeedsRequest, parseResult.getAdListJson())
            parseResult.setAdHolder(getAdCtrl()?.adHolder)

            requestEnv.parserCost = getCurTimeMillis() - requestEnv.parserStartTime

            return@updateParser parseResult
        }

        return netBuilder
    }

    private fun FlexCtrl.innerBuildNetworkBuilder(
        requestEnv: FeedsRequestEnv,
        dataRequest: DataRequest
    ): NetworkBuilder<*> {
        val dataEnv = requestEnv.dataEnv
        val repo = dataRepo()

        // 忽略 dataRepo 逻辑，强行请求：
        if (dataRequest.forceRequestIgnoreDataRepo) {
            return dataRequest.buildDefaultNetworkBuilder(dataEnv)
        }

        // 优先使用 dataRepo 进行创建（有一次拦截的机会）：
        val repoNetworkBuilder = if (dataEnv.refreshForward == ListRefreshForward.RESET) {
            if (dataEnv.refreshAction == ListRefreshAction.PRELOAD) {
                repo.createPreloadRequest(dataRequest, dataEnv)
                    ?: repo.createResetRequest(dataRequest, dataEnv) // 如果没定义预载接口，默认是首刷的
            } else {
                repo.createResetRequest(dataRequest, dataEnv)
            }
        } else {
            repo.createOtherRequest(dataRequest, dataEnv)
        }
        if (repoNetworkBuilder != null) {
            return repoNetworkBuilder
        }

        // dataRepo 未返回的话使用 DataRequest 默认创建：
        return dataRequest.buildDefaultNetworkBuilder(dataEnv)
    }

}