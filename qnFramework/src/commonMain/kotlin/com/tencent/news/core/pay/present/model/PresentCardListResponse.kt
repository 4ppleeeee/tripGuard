package com.tencent.news.core.pay.present.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure
import kotlinx.serialization.Serializable

@Serializable
data class PresentCardListResponse(
    val code: Int? = 0,
    val msg: String? = null,
    val data: PresentCardListData? = null
) : IKmmKeep, IKmmPure

@Serializable
data class PresentCardListData(
    val orders: List<PresentOrder> = emptyList(),
    val has_more: Boolean = true
) : IKmmKeep, IKmmPure

@Serializable
data class PresentOrder(
    val price: Int? = null, // 支付价格，单位为分
    val member_type: String? = null, // 订单类型，直接用来外显（点号后面）
    val pay_channel: String? = null, // 支付类型外显
    val order_id: String? = null, // 外显的订单号
    val encrypted_order_id: String? = null, // 仅主态可获取的加密订单号，生成领取链接/查询礼物时使用这个传入
    val gift_status: Int = 0, // 1 待赠送 2 已领取 3 已过期
    val pay_time: String? = null,
    val expire_time: String? = null,
    val receive_time: String? = null,
    val recipient: PresentCardUserInfo? = null,
    val sender: PresentCardUserInfo? = null,
    val product_info: ProductInfo? = null,
    val product_type: String? = null // member/column
) : IKmmKeep, IKmmPure

@Serializable
data class ProductInfo(
    val id: String? = null, // 专栏id或者会员作者suid
    val nick: String? = null, // 作者名字/专栏标题
    val pic_url: String? = null,
    val media_id: String? = null,
    val media_suid: String? = null,
    val vip_icon: String? = null,
    val vip_icon_night: String? = null,
    val jumpurl: String? = null

) : IKmmKeep, IKmmPure

@Serializable
data class PresentCardUserInfo(
    val suid: String? = null,
    val nick: String? = null,
    val head_url: String? = null,
    val vip_icon: String? = null,
    val vip_icon_night: String? = null,
) : IKmmKeep, IKmmPure

/**
 * 礼物状态常量
 */
object GiftStatus {
    const val PENDING = 1    // 待赠送
    const val RECEIVED = 2   // 已领取
    const val EXPIRED = 3    // 已过期
}

object ProductType {
    const val COLUMN = "column"
    const val MEMBER = "member"
    const val PACKAGE = "package"
}

 