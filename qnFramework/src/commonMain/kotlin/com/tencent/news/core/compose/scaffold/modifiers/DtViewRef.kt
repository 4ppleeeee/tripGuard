package com.tencent.news.core.compose.scaffold.modifiers

import com.tencent.news.core.dt.constants.DtElementId

// 主动上报大同点击标识
const val REPORT_DATONG_ELEMENT_CLICK_MANUAL = "news_datong_element_click_manual"

// 主动上报大同曝光标识
const val REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL = "news_datong_element_exposure_manual"

/**
 * 大同上报视图引用接口，用于跨模块传递视图引用（避免业务层直接依赖 qnView 的 ViewRef）
 * 实现由 qnView 层通过 [DtCurrentView.toRef] 提供
 */
interface IDtViewRef {
    fun setPropForce(propKey: String, propValue: ElementReportParams)

    fun nativeRef(): Int? = null
}

/**
 * 主动上报元素点击（IDtViewRef 版本，可在 qnUser/qnMedia 等业务层使用）
 * @param view 通过 DtCurrentView.toRef() 转换的视图引用
 * @param elementId 元素id
 * @param params 元素参数
 */
fun reportElementClickEvent(
    view: IDtViewRef?,
    elementId: DtElementId,
    params: Map<String, Any>? = null,
    logicParentView: IDtViewRef? = null,
) {
    view?.setPropForce(
        REPORT_DATONG_ELEMENT_CLICK_MANUAL,
        ElementReportParams(
            elementId = elementId,
            params = params,
            logicParentViewRef = { logicParentView?.nativeRef() },
        )
    )
}

/**
 * 主动上报元素曝光（IDtViewRef 版本，可在 qnUser/qnMedia 等业务层使用）
 * @param view 通过 DtCurrentView.toRef() 转换的视图引用
 * @param elementId 元素id
 * @param params 元素参数
 */
fun reportElementExposureEvent(
    view: IDtViewRef?,
    elementId: DtElementId,
    params: Map<String, Any>? = null
) {
    view?.setPropForce(
        REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL,
        ElementReportParams(elementId = elementId, params = params)
    )
}
