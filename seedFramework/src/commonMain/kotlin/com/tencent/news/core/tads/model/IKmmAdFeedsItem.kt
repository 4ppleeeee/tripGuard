package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.api.IExposure
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.IKmmDeepClone
import kotlinx.serialization.Serializable

typealias IAdListItem = IKmmAdFeedsItem // 简化命名

/**
 * 信息流广告item接口
 */
interface IKmmAdFeedsItem : IKmmFeedsItem, IExposure, IKmmDeepClone {
    val adItemEnv: KmmAdItemEnv
}

fun IKmmFeedsItem?.isAdFeedsItem(): Boolean = this is IKmmAdFeedsItem


@Serializable
class KmmAdItemEnv : IKmmKeep {

    // 信息流item上，保存个原始订单数据
    var adOrder: IKmmAdOrder? = null

    private var _index = 1
    var index: Int
        get() = orderEnv()?.index ?: _index
        set(value) {
            orderEnv()?.index = value
            _index = value
        }

    var isInserted: Boolean = false

    var isOriginExposed: Boolean = false        // 是否原始曝光过
        set(value) {
            field = value
            orderEnv()?.isOriginExposed = value
        }
    var isRealExposed: Boolean = false          // 是否真实曝光过
        set(value) {
            field = value
            orderEnv()?.isRealExposed = value
        }

    var breakageReason: String = ""             // 订单折损原因

    private fun orderEnv(): KmmAdOrderEnv? = adOrder?.env

}