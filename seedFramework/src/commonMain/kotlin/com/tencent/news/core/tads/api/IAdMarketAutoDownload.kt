package com.tencent.news.core.tads.api

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.platform.api.BaseEvent
import com.tencent.news.core.tads.model.AdJumpMarketType
import com.tencent.news.core.tads.model.IKmmAdOrder

data class AdMarketAutoDownloadParam(
    val context: IKmmContext,
    val order: IKmmAdOrder?,
    val traceStr: String? = null,
    val callback: IAdMarketAutoDownloadCallback? = null,
    val scene: AdMarketAutoDownloadFrom
)

interface IAdMarketAutoDownloadCallback {
    fun onDownloadStateChange(status: Int) {}
    fun onSuccess() {}
    fun onError(error: AdMarketAutoDownloadError) {}
    fun onRelease() {}
}

enum class AdMarketAutoDownloadError(val code: Int, val needDowngrade: Boolean) {
    ORDER_NULL(500, true),           // 订单为空
    DATA_NULL(501, true),            // jump_android_market字段为空
    SCHEME_NULL(503, true),          // 未下发厂商下载scheme
    MARKET_NOT_FIND(504, true)       // 未找到应用商城
}

enum class AdMarketAutoDownloadFrom(val code: Int) {
    H5(AdJumpMarketType.XIJING),
    NATIVE(AdJumpMarketType.TERMINAL);
}

data class AppInstallSuccessEvent(
    val packageName: String
) : BaseEvent(), IKmmKeep