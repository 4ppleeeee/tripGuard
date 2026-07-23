package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.getQueryParam
import com.tencent.news.core.extension.takeIfNotBlank
import com.tencent.news.core.tads.constants.AdHalfScreenCardType
import com.tencent.news.core.tads.constants.isWxConsulDt


fun IKmmAdOrder.getAdOid(): String = info.oid

fun IKmmAdOrder.getAdLoid(): Int = adIndex.loid

fun IKmmAdOrder.getAdSeq(): Int = adIndex.seq

fun IKmmAdOrder.getAdChannel(): String = adIndex.adChannel

fun IKmmAdOrder.getAdChannelId(): Int = adIndex.adChannelId

fun IKmmAdOrder.getNativePosType(): Int = action.nativePosType.value

fun IKmmAdOrder?.getLogKey(): String {
    this ?: return ""
    return "${adIndex.adChannel}/loid[${adIndex.loid}]"
}

fun IKmmAdOrder?.getLogMsg(): String {
    this ?: return "order is null"
    return "(loid=${getAdLoid()}|${getAdChannel()}|seq=${getAdSeq()}" +
            ",o=${getAdOid()}|${info.netLogId}|${info.soid},c=${info.cid},s=${adIndex.orderSource}" +
            ",act=${info.actType},sub=${info.subType}) ${info.title.take(16)}" +
            ",nativePosType=${getNativePosType()}"
}

fun IKmmAdOrder.getOrderUniqueKey(): String =
    "${getAdLoid()}_${getAdChannel()}_${getAdOid()}_${getAdSeq()}"

fun IKmmAdOrder.logMsgWithTrace(): String {
    return "${getLogMsg()}:trace_id=${optTraceId()},ams_traceid=${optAmsTraceId()}"
}

// 算法重排信息，后台没直接下发，需要从order的曝光url参数中取
fun IKmmAdOrder.getReRankingData(): String =
    report.viewReportUrl.getQueryParam("reranking_data")

// 按订单维度的traceId
fun IKmmAdOrder?.optTraceId(): String = this?.info?.traceId ?: ""

// 按刷次维度的traceId（一刷里所有订单这个值都一样，某些情况也称为requestId）
fun IKmmAdOrder?.optAmsTraceId(): String = this?.info?.amsTraceId ?: ""

fun IKmmAdOrder?.optAiState(): KmmAdAiState? = this?.env?.aiState

// 订单未曝光过
fun IKmmAdOrder.optNotExposed(): Boolean = !env.isOriginExposed && !env.isRealExposed

fun IKmmAdOrder?.optIsExposed(): Boolean {
    this ?: return false
    return env.isOriginExposed || env.isRealExposed
}

fun IKmmAdOrder?.optOpenScheme(): String = this?.action?.openScheme.getNonNull()

fun IKmmAdOrder?.optOpenPkg(): String {
    return this?.action?.openPkg?.takeIfNotBlank()
        ?: this?.downloadDto?.pkgName?.takeIfNotBlank()
        ?: ""
}

fun IKmmAdOrder?.enableWxConsultHalfCard(): Boolean {
    this ?: return false
    return isHalfCardType() && isWxConsulDt()
}

fun IKmmAdOrder?.isHalfCardType(): Boolean {
    this ?: return false
    return action.adHalfScreenCardInfo?.halfScreenCardType == AdHalfScreenCardType.COUNSEL
}

// 用来特殊判断微信客服 文案图标展示 & 跳转
// 7840 （新闻主端在官方页二跳客服链路支持外层点击行动按钮直跳）
fun IKmmAdOrder?.isWxConsultButtonDisplay(): Boolean {
    this ?: return false
    return action.isConsultDisplay
}

