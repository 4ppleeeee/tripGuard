package com.tencent.news.core.tads.model

import com.tencent.news.core.tads.constants.AdLoid
import com.tencent.news.core.tads.constants.INVALID_NUM


fun IKmmAdFeedsItem.getAdOrder(): IKmmAdOrder? = adItemEnv.adOrder

fun IKmmAdFeedsItem.getAdSeq(): Int = getAdOrder()?.getAdSeq() ?: INVALID_NUM

fun IKmmAdFeedsItem.bindAdOrder(adOrder: IKmmAdOrder?) {
    adItemEnv.adOrder = adOrder
}

fun IKmmAdFeedsItem.optSetRealExposed(exposed: Boolean) {
    adItemEnv.isRealExposed = exposed
    adItemEnv.adOrder?.env?.isRealExposed = exposed
}

fun IKmmAdFeedsItem.optSetOriginExposed(exposed: Boolean) {
    adItemEnv.isOriginExposed = exposed
    adItemEnv.adOrder?.env?.isOriginExposed = exposed
}

fun IKmmAdFeedsItem.getAdScene(): AdScene {
    return AdScene(
        majorLoid = getAdOrder()?.getAdLoid() ?: AdLoid.NONE,
        adChannel = getAdOrder()?.getAdChannel() ?: ""
    )
}


fun IKmmAdFeedsItem?.isPlayableMiniGame(): Boolean {
    return getPlayableMiniGameInfo() != null
}

fun IKmmAdFeedsItem?.getPlayableMiniGameInfo(): IAdPlayableMiniGameInfo? {
    this ?: return null
    val order = getAdOrder() ?: return null
    return order.info.playableMiniGameInfo
}