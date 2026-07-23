package com.tencent.news.core.list.controller

import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.controller.FlexCtrlWidgetHelper.findLastNewsListWidget
import com.tencent.news.core.list.model.ItemEventEx.eventId
import com.tencent.news.core.list.model.RequestCgi
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.IndexWidget
import com.tencent.news.core.page.model.RequestType
import com.tencent.news.core.page.model.StructWidgetEx.findSingleWidget
import com.tencent.news.core.page.model.pickInitRequest
import com.tencent.news.core.page.model.pickOne

// 【重要】列表网络请求 DataRequest 构建逻辑
// @see 下一步 NetworkBuilder 逻辑见：FlexCtrlNetworkHelper
internal object FlexCtrlRequestHelper {

    fun FlexCtrl.createResetDataRequest(requestEnv: FeedsRequestEnv): DataRequest {
        val refreshAction = requestEnv.getRefreshAction()

        if (refreshAction == ListRefreshAction.PAGE_INDEX) {
            createIndexPageResetRequest(requestEnv)?.let { return it }
        }

        // '话题专题'-最新/最热
        val isTopicRequest = refreshAction in setOf(
            ListRefreshAction.RESET_TOPIC_EVENT_LATEST,
            ListRefreshAction.RESET_TOPIC_EVENT_HOTTEST
        )
        if (isTopicRequest) {
            return createTopicResetRequest(refreshAction)
        }

        return createDefaultResetRequest()
    }

    private fun FlexCtrl.createIndexPageResetRequest(requestEnv: FeedsRequestEnv): DataRequest? {
        val indexKey = requestEnv.dataEnv.refreshActionData?.indexKey?.takeIf { it.isNotBlank() }
            ?: return null
        val indexWidget = rootWidget.findSingleWidget<IndexWidget> { it.widget_id == indexKey }
            ?: return null
        return indexWidget.action?.page_reset?.pickClickRequest()
            ?: indexWidget.action?.page_reset?.pickAnyRequest()
    }

    private fun FlexCtrl.createDefaultResetRequest(): DataRequest {
        // 后台下发 ChannelWidget 可以指定请求接口及参数（专题多tab使用）
        return rootWidget.pager?.mainChannel.pickInitRequest()
            ?: DataRequest().apply { service = RequestCgi.CHANNEL_FEED }
    }

    // '话题专题'-最新/最热
    private fun FlexCtrl.createTopicResetRequest(action: ListRefreshAction): DataRequest {
        val pageItem = pageItem?.invoke()

        val defaultRequest = DataRequest().apply {
            service = "/gw/page/event_detail_more_weibo"
            type = RequestType.REQUEST
            reqdata = mutableMapOf<String, String>().apply {
                put("eventId", pageItem.eventId)
                put("vote_id", pageItem?.ctxDto?.eventVoteId.getNonNull())
                put("size", "10")
            }
            forceRequestIgnoreDataRepo = true // 强制替换repo默认的 event_detail 接口
        }

        // 参考分页接口，给后台一个修改数据的能力
        val remoteRequest = findLastNewsListWidget()?.action?.load_more?.request?.pickOne()
        if (remoteRequest != null) {
            if (remoteRequest.host.isNotNullOrEmpty()) {
                defaultRequest.host = remoteRequest.host
            }
            if (remoteRequest.service.isNotNullOrEmpty()) {
                defaultRequest.service = remoteRequest.service
            }
            if (!remoteRequest.reqdata.isNullOrEmpty()) {
                defaultRequest.reqdata = remoteRequest.reqdata
            }
        }

        // 确保首刷是page=1，sort_type正确
        defaultRequest.reqdata?.apply {
            put("page", "1")
            put("sort_type", action.getSortType())
        }
        return defaultRequest
    }

    private fun ListRefreshAction.getSortType(): String =
        if (this == ListRefreshAction.RESET_TOPIC_EVENT_LATEST) "2" else "1"

}