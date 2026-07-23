package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.QnFrameworkLogic


@OptIn(KmmInternalApi::class)
fun dtReport() = QnFrameworkLogic.dtReport

interface IAppDtReport {

    /**
     * 获取[IKmmFeedsItem]的上报参数
     */
    fun getFeedsItemReportParams(feedsItem: IKmmFeedsItem): Map<String, Any>

    /**
     * 获取[IKmmFeedsItem]的动态上报参数
     */
    fun getFeedsItemDynamicParams(feedsItem: IKmmFeedsItem): DynamicParamsProvider?

    /**
     * 上报点击(无法hook)
     */
    fun reportElementClickEvent(element: Any)

    /**
     * 主动上报点击事件
     */
    fun reportElementClickEvent(
        view: Any,
        elementId: String,
        params: Map<String, Any>? = null,
    )

    /**
     * 主动上报点击事件，支持指定逻辑父节点
     */
    fun reportElementClickEvent(
        view: Any,
        elementId: String,
        params: Map<String, Any>?,
        logicParentView: Any?,
    ) {
        if (logicParentView == null) {
            reportElementClickEvent(view, elementId, params)
            return
        }
        setElementReportInfo(
            element = view,
            elementId = elementId,
            identifier = "",
            params = params,
            logicParentView = logicParentView
        )
        reportElementClickEvent(view)
    }

    /**
     * 主动上报曝光事件
     */
    fun reportElementExposureEvent(
        view: Any,
        elementId: String,
        params: Map<String, Any>? = null,
    )

    /**
     * 设置页面参数
     */
    fun setPageReportInfo(
        page: Any,
        pageId: String,
        contentId: String,
        params: Map<String, Any>?,
        dynamicParams: DynamicParamsProvider? = null,
    )

    /**
     * 设置元素参数
     */
    fun setElementReportInfo(
        element: Any,
        elementId: String,
        identifier: String,
        enableExposure: Boolean = false,
        enableExposureEnd: Boolean = false,
        minExposureRatio: Float = 0.01F,
        params: Map<String, Any>? = null,
        dynamicParams: DynamicParamsProvider? = null,
        logicParentView: Any? = null,
    )

    fun reportBizEvent(element: Any?, eventId: String, params: Map<String, Any>?)

    fun traversePage(view: Any)

    fun pageLogicDestroy(view: Any)

    fun reset4Reuse(view: Any) {}
}
