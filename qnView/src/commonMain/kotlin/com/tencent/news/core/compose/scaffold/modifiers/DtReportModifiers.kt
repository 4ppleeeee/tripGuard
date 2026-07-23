package com.tencent.news.core.compose.scaffold.modifiers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose_dsl.kuikly.extension.nativeRef
import com.tencent.kuikly.compose_dsl.kuikly.extension.setProp
import com.tencent.kuikly.core.base.Props
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.news.core.dt.constants.DtBizEvent
import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.dt.constants.DtPageId
import com.tencent.news.core.dt.constants.DtParamKey
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.PageDtReport
import com.tencent.news.core.platform.api.DynamicParamsProvider
import com.tencent.news.core.platform.api.dtReport

/**
 * [参考文档](https://doc.weixin.qq.com/doc/w3_AbsAnAadABsvgP8JYMTRMWmNdGiQ1?scode=AJEAIQdfAAo6DYAVoVAbsAnAadABs)
 */

// 页面上报标识
internal const val DATONG_PAGE = "datong_page"

// 元素上报标识
internal const val DATONG_ELEMENT = "datong_element"


// 主动上报大同自定义事件
internal const val REPORT_DATONG_BIZ_EVENT_MANUAL = "news_datong_biz_event_manual"

// 主动触发曝光检测
internal const val REPORT_DATONG_TRAVERSE_PAGE = "news_datong_traverse_page"
internal const val REPORT_DATONG_PAGE_LOGIC_DESTROY = "news_datong_page_logic_destroy"

// 元素逻辑父节点
typealias DtLogicParentView = ViewRef<*>

// 当前元素节点
typealias DtCurrentView = ViewRef<*>

fun DtCurrentView.toRef(): IDtViewRef {
    val viewRef = this
    return object : IDtViewRef {
        override fun setPropForce(propKey: String, propValue: ElementReportParams) {
            viewRef.view?.getViewAttr()?.setPropForce(propKey, propValue)
        }

        override fun nativeRef(): Int? = viewRef.nativeRef
    }
}

/**
 * 主动上报元素点击，可参考[FollowBtnViewModel.reportDatong]
 *
 * 如果想在VM中调用，可使用[com.tencent.news.core.compose.scaffold.modifiers.DtViewRefKt.reportElementClickEvent]
 *
 * @param view 当前compose节点
 * @param elementId 元素id
 * @param params 元素参数
 */
fun reportElementClickEvent(
    view: DtCurrentView?,
    elementId: DtElementId,
    params: Map<String, Any>? = null,
    logicParentView: DtLogicParentView? = null,
) {
    view?.view?.getViewAttr()?.setPropForce(
        REPORT_DATONG_ELEMENT_CLICK_MANUAL,
        ElementReportParams(
            elementId = elementId,
            params = params,
            logicParentViewRef = { logicParentView?.nativeRef },
        )
    )
}

/**
 * 主动上报元素点击，可参考[FollowBtnViewModel.reportDatong]
 * @param view 当前compose节点
 * @param event 自定义事件
 * @param params 事件参数
 */
fun reportBizEvent(
    view: DtCurrentView?,
    event: DtBizEvent,
    params: Map<String, Any>? = null
) {
    view?.view?.getViewAttr()?.setPropForce(
        REPORT_DATONG_BIZ_EVENT_MANUAL,
        BizEventParams(event = event, params = params).asReportInfo()
    )
}

fun traversePage(view: DtCurrentView?) {
    view?.view?.getViewAttr()?.setPropForce(REPORT_DATONG_TRAVERSE_PAGE, view)
}

fun pageLogicDestroy(view: DtCurrentView?) {
    view?.view?.getViewAttr()?.setPropForce(REPORT_DATONG_PAGE_LOGIC_DESTROY, view)
}

private fun Props.setPropForce(propKey: String, propValue: Any) {
    // 只能在非主线程更新prop
    setTimeout {
        forceUpdate = true
        setProp(propKey, propValue)
        forceUpdate = false
    }
}

/**
 * 主动上报元素曝光
 * @param view 当前compose节点
 * @param elementId 元素id
 * @param params 元素参数
 */
fun reportElementExposureEvent(
    view: DtCurrentView?,
    elementId: DtElementId,
    params: Map<String, Any>? = null
) {
    view?.view?.getViewAttr()?.setPropForce(
        REPORT_DATONG_ELEMENT_EXPOSURE_MANUAL,
        ElementReportParams(elementId = elementId, params = params)
    )
}

/**
 * 绑定文章元素信息
 * @param feedsItem 文章信息
 * @param minExposureRatio 曝光阈值，默认0.01F，曝光比例达到该值后上报曝光
 */
fun Modifier.dtArticleElement(
    feedsItem: IKmmFeedsItem,
    minExposureRatio: Float = 0.01F,
): Modifier {
    return dtElement(
        elementId = DtElementId.ArticleCard,
        identifier = listOf(
            feedsItem.ctxDto.newsChannel,
            feedsItem.baseDto.idStr,
            feedsItem.baseDto.articleType
        ).joinToString("-"),
        enableExposure = true,
        enableExposureEnd = false,
        minExposureRatio = minExposureRatio,
        elementParams = dtReport()?.getFeedsItemReportParams(feedsItem),
        dynamicParamsProvider = dtReport()?.getFeedsItemDynamicParams(feedsItem)
    )
}

/**
 * 绑定模块级文章元素信息
 * @param feedsItem 文章信息
 * @param minExposureRatio 曝光阈值，默认0.01F，曝光比例达到该值后上报曝光
 */
fun Modifier.dtModArticleElement(
    feedsItem: IKmmFeedsItem,
    minExposureRatio: Float = 0.01F,
): Modifier {
    return dtElement(
        elementId = DtElementId.EM_MOD_ARTICLE,
        identifier = feedsItem.exportUniqueKey,
        enableClick = false,
        enableExposure = true,
        enableExposureEnd = false,
        minExposureRatio = minExposureRatio,
        elementParams = dtReport()?.getFeedsItemReportParams(feedsItem),
        dynamicParamsProvider = dtReport()?.getFeedsItemDynamicParams(feedsItem)
    )
}

// 1、禁止传入 null 的 elementId 信息
// 2、参数只支持字符串，提前防止json异常
fun Modifier.dtElementWithSafeCheck(
    elementId: DtElementId,
    identifier: String = "",
    minExposureRatio: Float = 0.01F,
    enableClick: Boolean = true,
    enableExposure: Boolean = false,
    enableExposureEnd: Boolean = false,
    elementParams: Map<String, String>? = null,
    dynamicParamsProvider: DynamicParamsProvider? = null,
    logicParentView: DtLogicParentView? = null,
): Modifier {
    return this.dtElement(
        elementId = elementId,
        identifier = identifier,
        minExposureRatio = minExposureRatio,
        enableClick = enableClick,
        enableExposure = enableExposure,
        enableExposureEnd = enableExposureEnd,
        elementParams = elementParams,
        dynamicParamsProvider = dynamicParamsProvider,
        logicParentView = logicParentView
    )
}

/**
 * 绑定大同元素信息
 * @param elementId 元素id
 * @param elementParams 元素参数
 * @param identifier 元素唯一标识（列表元素必须设置，否则复用可能出问题）
 * @param minExposureRatio 曝光阈值，默认0.01F，曝光比例达到该值后上报曝光
 * @param enableExposure 是否开启曝光上报
 * @param enableExposureEnd 是否开启曝光结束上报
 * @param dynamicParamsProvider 动态参数
 * @param logicParentView 逻辑父节点
 */
fun Modifier.dtElement(
    elementId: DtElementId?,
    identifier: String = "",
    minExposureRatio: Float = 0.01F,
    enableClick: Boolean = true,
    enableExposure: Boolean = false,
    enableExposureEnd: Boolean = false,
    elementParams: Map<String, Any>? = null,
    dynamicParamsProvider: DynamicParamsProvider? = null,
    logicParentView: DtLogicParentView? = null,
): Modifier {
    elementId ?: return this
    val dt = ElementReportParams(
        elementId,
        identifier,
        enableClick,
        enableExposure,
        enableExposureEnd,
        minExposureRatio,
        elementParams,
        dynamicParamsProvider,
        { logicParentView?.nativeRef }
    )
//    return setProp(
//        DATONG_ELEMENT,
//        propArg0 = dt.asReportInfo(),
//        viewPropUpdater = ComposeLayoutPropUpdater
//    )
    return this.dtElement(dt)
}

fun Modifier.dtElement(elementReportParams: ElementReportParams?): Modifier {
    val dt = elementReportParams ?: return this
    return this.setProp(
        key = DATONG_ELEMENT,
        value = dt.asReportInfo(),
    )
}

/**
 * 声明式绑定大同元素，并在节点首次可用或 [traversalKey] 变化时重新触发曝光检测。
 */
@Composable
fun Modifier.dtElementWithRetraversal(
    elementReportParams: ElementReportParams?,
    traversalKey: Any?,
): Modifier {
    var currentView by remember { mutableStateOf<DtCurrentView?>(null) }
    val enableRetraversal = elementReportParams?.enableExposure == true
    LaunchedEffect(currentView, traversalKey, enableRetraversal) {
        if (enableRetraversal) {
            currentView?.let { traversePage(it) }
        }
    }
    return dtElement(elementReportParams).nativeRef { currentView = it }
}

/**
 * 绑定需要主动点击上报的大同元素，曝光仍交给大同可见性检测。
 */
@Composable
fun Modifier.manualElementReport(
    elementId: DtElementId,
    identifier: Any? = elementId,
    enableExposure: Boolean = false,
    exposureParams: Map<String, Any>? = null,
    exposureDynamicParamsProvider: DynamicParamsProvider? = null,
    enableClick: Boolean = true,
    clickParams: Map<String, Any>? = null,
    onClick: (() -> Unit)? = null
): Modifier {
    val needViewRef = enableClick && onClick != null
    var currentView by remember(identifier) { mutableStateOf<DtCurrentView?>(null) }
    val reportModifier = dtElement(
        elementId = elementId,
        identifier = identifier?.toString().orEmpty(),
        enableClick = false,
        enableExposure = enableExposure,
        elementParams = exposureParams.takeIf { enableExposure && exposureDynamicParamsProvider == null },
        dynamicParamsProvider = exposureDynamicParamsProvider.takeIf { enableExposure }
    )

    val refModifier = if (needViewRef) {
        reportModifier.nativeRef { currentView = it }
    } else {
        reportModifier
    }
    return if (enableClick && onClick != null) {
        refModifier.clickable {
            reportElementClickEvent(
                view = currentView,
                elementId = elementId,
                params = clickParams
            )
            onClick()
        }
    } else {
        refModifier
    }
}

/**
 * 绑定文章详情页大同页面信息
 * @param item 文章详情页数据
 * @param channelId 频道id
 * @param isLandingPage 是否落地页
 */
fun Modifier.dtPageDetail(
    item: IKmmFeedsItem,
    channelId: String,
    isLandingPage: Boolean,
    dynamicParamsProvider: DynamicParamsProvider? = null
): Modifier {
    // todo item动态参数，页面动态参数
    return dtPage(
        pageId = DtPageId.ARTICLE_DETAIL,
        channelId = channelId,
        contentId = item.baseDto.idStr,
        isLandingPage = isLandingPage,
        pageParams = dtReport()?.getFeedsItemReportParams(item),
        dynamicParamsProvider = dynamicParamsProvider
    )
}

fun Modifier.dtPageIf(
    predict: Boolean,
    pageId: DtPageId,
    channelId: String,
    contentId: String = "",
    logicDestroy: Boolean = false,
    isLandingPage: Boolean = false,
    pageParams: Map<String, Any>? = null,
    dynamicParamsProvider: DynamicParamsProvider? = null
): Modifier {
    return if (predict) {
        dtPage(
            pageId,
            channelId,
            contentId,
            logicDestroy,
            isLandingPage,
            pageParams,
            dynamicParamsProvider
        )
    } else {
        this
    }
}

/**
 * 绑定大同页面信息
 *
 * @param pageId 页面id
 * @param channelId 频道id
 * @param contentId 内容id
 * @param isLandingPage 是否落地页
 * @param pageParams 页面参数
 */
fun Modifier.dtPage(
    pageId: DtPageId,
    channelId: String,
    contentId: String = "",
    logicDestroy: Boolean = false,
    isLandingPage: Boolean = false,
    pageParams: Map<String, Any>? = null,
    dynamicParamsProvider: DynamicParamsProvider? = null
): Modifier {
    val params = mutableMapOf<String, Any>(
        DtParamKey.CHANNEL_ID to channelId,
        DtParamKey.IS_LANDING_PAGE to if (isLandingPage) 1 else 0
    )

    pageParams?.let {
        params.putAll(it)
    }

    val dt = PageReportParams(pageId, contentId, logicDestroy, params, dynamicParamsProvider)
    return this.setProp(
        key = DATONG_PAGE,
        value = dt.asReportInfo(),
    )
}

fun Modifier.dtPage(dtReport: PageDtReport?, contentId: String = ""): Modifier {
    dtReport ?: return this
    return this.dtPage(
        pageId = dtReport.pageId,
        channelId = dtReport.channelId,
        contentId = contentId,
        isLandingPage = dtReport.isLandingPage,
        pageParams = dtReport.pageParams,
        dynamicParamsProvider = dtReport.dynamicParams
    )
}
