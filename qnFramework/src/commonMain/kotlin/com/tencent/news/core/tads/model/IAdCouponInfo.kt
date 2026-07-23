package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep

/**
 * 优惠券一级类目枚举
 */
object CouponCategory {
    const val CASH = 1          // 现金减免券
    const val DISCOUNT = 2      // 折扣比例券
    const val VIRTUAL = 3       // 虚拟资产券
    const val EXCHANGE = 4      // 权益兑换券
}

/**
 * 电商通用-优惠券信息
 */
interface IAdCouponInfo : IKmmKeep {
    val firstCategory: Int          // 一级行业类目 @CouponCategory
    val couponNameText: String      // 券名称（如 "购物红包"）
    val thresholdText: String       // 门槛文案（如 "满200元减"）
    val discountValue: String       // 优惠金额（如 "100"）
    val discountUnit: String        // 优惠单位（如 "元"）

    /** 按券类型校验必填字段 */
    fun isValid(): Boolean = when (firstCategory) {
        CouponCategory.CASH, CouponCategory.DISCOUNT -> {
            // 现金券/折扣券：金额 + 单位 + 门槛 必填（券名非必填，有兜底）
            discountValue.isNotEmpty() && discountUnit.isNotEmpty() && thresholdText.isNotEmpty()
        }

        CouponCategory.EXCHANGE -> {
            // 权益券：券名 + 金额 + 单位 必填（无门槛）
            couponNameText.isNotEmpty() && discountValue.isNotEmpty() && discountUnit.isNotEmpty()
        }

        else -> false
    }
}
