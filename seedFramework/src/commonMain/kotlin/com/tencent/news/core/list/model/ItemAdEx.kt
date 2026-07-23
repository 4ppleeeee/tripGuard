package com.tencent.news.core.list.model

import com.tencent.news.core.list.api.IContextDtoHolder
import com.tencent.news.core.tads.constants.AdNativeStyleType
import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import com.tencent.news.core.tads.model.getAdOrder

object ItemAdEx {
    fun IContextDtoHolder?.isAd(): Boolean = this is IKmmAdFeedsItem
    fun IContextDtoHolder?.isNativeAd(): Boolean = this?.ctxDto?.nativeAd != null
    fun IContextDtoHolder?.isNativeAdLive(): Boolean {
        if (!isNativeAd()) return false
        val adOrder = this?.ctxDto?.nativeAd?.getAdOrder()
        return adOrder?.action?.nativeStyleType ==
                AdNativeStyleType.CHANNELS_LIVE_STREAM
    }
}