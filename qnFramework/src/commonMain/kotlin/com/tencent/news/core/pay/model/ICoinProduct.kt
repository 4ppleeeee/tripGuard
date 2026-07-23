package com.tencent.news.core.pay.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable

/**
 * 当前专栏价格(钻石/代币)
 */
@Suppress("AnnotationOnSeparateLine")
typealias QnCoinProduct = @Serializable(ICoinProduct.QnSerializer::class) ICoinProduct

interface ICoinProduct : IKmmParcelable {
    var idStr: String           // 物品id(米大师物品ID)，支付时需要携带
    var name: String            // 代币物品名称，如专栏名称
    var coins: Int              // 购买代币数量
    var presentCoins: Int       // 赠送代币； 当前安卓会赠送代币；代币总量需要累加 coins
    var price: Int              // 原价，单位：分，例如：262 表示 2.62 元
    var priceInCents: Int       // 价格，单位分，目前仅用于直播业务
    val discountPrice: Int      // 折扣价，单位：分，例如：79 表示 0.79 元
    val discountStartTime: Long // 折扣开始时间，时间戳（秒），0 表示无折扣时间限制
    val discountEndTime: Long   // 折扣结束时间，时间戳（秒），0 表示无折扣时间限制
    val discountStatus: Int     // 折扣状态，0：无折扣或折扣已结束，1：折扣进行中
    var coinProductType: Int    // 0:未知，1:专栏，2:专栏单篇文章，3:文章推广/加热，6:礼包类商品
    val displayInfo: IDisplayInfo?
    val offerId: String?        // 米大师offerId，用于支付，目前暂时只有礼包在用
    var scene: Int
    val singleText: String?     // 单篇按钮角标文案

    fun totalCoins(): Int                           // 代币总数，含赠送代币
    fun realCostPrice(): Int                        // 真实购买价格，内部会根据折扣活动等返回真实需要支付的金额
    fun realCostPriceInCents(): Int                 // 真实购买价格，单位分
    fun hasColumnDiscountActivity(): Boolean        // 是否有专栏折扣活动
    fun hasValidColumnDiscountActivity(): Boolean   // 是否有有效的专栏折扣活动

    object QnSerializer : QnInterfaceSerializer<ICoinProduct>(ICoinProduct::class)

    companion object : IQnInterfaceCreator<ICoinProduct> {
        override fun defaultSerializer() = QnSerializer
        fun safeDecode(json: String): ICoinProduct? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: ICoinProduct): String = KtJson.safeEncode(QnSerializer, data)
    }
}