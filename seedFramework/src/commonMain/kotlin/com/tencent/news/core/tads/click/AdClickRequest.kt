@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.tads.click

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.service.FrameworkServiceBridge
import com.tencent.news.core.tads.constants.AdGdtClickActType
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import com.tencent.news.core.tads.model.IKmmAdOrder
import com.tencent.news.core.tads.model.getAdOrder

data class AdClickRequest constructor(
    var item: IKmmAdFeedsItem,
    val parentItem: IKmmAdFeedsItem? = null, // 子订单跳转的情况，需要使用父订单进行换链（ams订单用，cmp的不用）
    val actType: AdGdtClickActType = AdGdtClickActType.DEFAULT_CLICK,
    val extras: Map<String, String>? = null,
    var interceptor: IAdClickInterceptor? = null
) {
    val adOrder: IKmmAdOrder? get() = item.getAdOrder()

    // 以下是冷门功能：
    var context: IKmmContext? = null    // 本次路由绑定的context，主要安卓宿主用
    var qaIndex: Int = INVALID_NUM      // 用于问答组件（动态化半屏卡、问答轮播组件）点击时获取对应角标的跳转链接
    var doNotReportClick = false        // 本次跳转，不上报ssp点击（目前用于贴片广告）
    var useAsyncClickReport = false     // 异步点击上报标识
    var dynamicClickParams: Map<String, String>? = null // 动态化点击参数
    var jumpDirectly = false            // 直接转发给宿主路由，不做任何处理
    var isTab2InsertAd = false          // 信息流横版视频广告点击跳tab2并插入到tab2
    var forceAsyncReport = false        // 强制走异步上报逻辑，不用校验dest_url等参数（普遍用于子订单的情况）
    var exchangeOpenScheme: String = "" // 替换本次外跳scheme
    var hasReportGdtClick = false       // 是否已经上报计费点击

    var reportData: AdClickChainReportData = AdClickChainReportData() // 上报数据

    class AdClickChainReportData {
        var startTime: Long = 0L                                       // 开始时间
        var endProcessorType = AdClickProcessorType.UNKNOWN            // endChain Type
        var endProcessorTypeStr: String = ""                           // endChain String
        var success: Boolean = false                                   // true：代表kmm链路内处理了跳转
        var resultMsg: String = ""                                     // 错误信息
        fun costTime(): Long = getCurTimeMillis() - startTime          // 消耗时间

        override fun toString(): String =
            "最后执行链路[${endProcessorTypeStr.ifEmpty { "$endProcessorType" }}]：${success}-${resultMsg}，耗时：${costTime()}ms"

    }

    fun forceAsyncReportWithDestUrl() {
        val destUrl = adOrder?.action?.destUrl
        if (destUrl.isNotNullOrEmpty()) {
            forceAsyncReport = true
        }
    }

    fun reportGdtClick() {
        if (hasReportGdtClick) {
            return
        }
        hasReportGdtClick = true
        FrameworkServiceBridge.impl.reportAdClick(adOrder, actType, AdReportConfig.onlyGdt())
    }

    fun getDebugMsg(): String {
        return "广告点击[${actType}]" +
                (if (qaIndex != INVALID_NUM) ", qaIndex=${qaIndex}" else "")
    }

    companion object {

        @kotlin.jvm.JvmStatic
        @kotlin.jvm.JvmOverloads
        fun create(
            item: IKmmAdFeedsItem,
            actType: AdGdtClickActType = AdGdtClickActType.DEFAULT_CLICK
        ) = AdClickRequest(
            item = item,
            actType = actType
        )

        @kotlin.jvm.JvmStatic
        @kotlin.jvm.JvmOverloads
        fun create(
            item: IKmmAdFeedsItem,
            extras: Map<String, String>,
            actType: AdGdtClickActType = AdGdtClickActType.DEFAULT_CLICK
        ) = AdClickRequest(
            item = item,
            actType = actType,
            extras = extras
        )

        @kotlin.jvm.JvmStatic
        fun create(
            item: IKmmAdFeedsItem,
            parentItem: IKmmAdFeedsItem
        ) = AdClickRequest(
            item = item,
            parentItem = parentItem
        )

        fun copyRequest(from: AdClickRequest, newItem: IKmmAdFeedsItem): AdClickRequest {
            if (from.item === newItem) {
                return from // 同一个item，不用copy
            }

            val result = from.copy(item = newItem)
            result.context = from.context
            result.qaIndex = from.qaIndex
            result.doNotReportClick = from.doNotReportClick
            result.useAsyncClickReport = from.useAsyncClickReport
            result.dynamicClickParams = from.dynamicClickParams
            result.reportData = from.reportData
            result.forceAsyncReport = from.forceAsyncReport
            return result
        }

        fun AdClickRequest.changeItem(newItem: IKmmAdFeedsItem): AdClickRequest =
            copyRequest(this, newItem)
    }
}

object AdClickExtrasKey {
    const val IS_FEED_REWARD = "isFeedReward"
}