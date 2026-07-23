package com.tencent.news.core.tads.model

/**
 * 超级蒙层配置
 */
interface IAdSuperMask {
    val disLeft: Int        // 左边距
    val disRight: Int       // 右边距
    val disUp: Int          // 上边距
    val disDown: Int        // 下边距
    val exposedRatio: Int       // 曝光比例
    val cardShowDuration: Long // 卡片展示时长
    var subOrders: List<IKmmAdOrder> // 子订单与父订单数据合并后的
    val originSubOrders: List<IKmmAdOrder> // 后台下发的原始子订单数据（数据不全）
}

fun IAdSuperMask?.isClickFrameValid(): Boolean {
    this ?: return false
    return disLeft > 0 || disRight > 0 || disUp > 0 || disDown > 0
}

