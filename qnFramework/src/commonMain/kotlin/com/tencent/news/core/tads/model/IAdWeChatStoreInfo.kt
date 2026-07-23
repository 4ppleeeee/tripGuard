package com.tencent.news.core.tads.model

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.extension.IKmmKeep

// 微信直购小店
interface IAdWeChatStoreInfo {

    val productImg: String?
    val productTitle: String?
    val productTags: List<String>?
    val totalSales: Int?
    val sellPrice: Int?
    val originalPrice: Int?
    val headImgUrl2c: String?       // 店铺头像
    val storeIconRType: Int?        // 小店品牌授权类型
    val storeIconTopType: Int?      // 小店好店类型（好店 > r标 > 不展示）
    val storeName: String?          // 店铺名称
    val couponQueryUrl: String?     // 优惠券查询链接
    val couponInfos: List<IWxShopCouponInfo>? // 优惠券信息
    val showPriceInfo: IWxShopShowPriceInfo? // 外显价格相关信息
    val subsidyLabelImg: IWxShopSubsidyLabelImg? // 补贴标签图片

    val storeIconFont: IconFont?    // 小店标识 IconFont

    val discountText: String        // 折扣文案

    fun isCrossShopActivityProduct(): Boolean

    fun isValid(): Boolean

}

/**
 * 微信直购小店优惠券类型。
 */
object WxShopCouponType {
    const val COUPON_TYPE_UNKNOWN = 0                  // 兜底类型
    const val COUPON_TYPE_PRODUCT_DISCOUNT = 1         // 商品条件折扣券
    const val COUPON_TYPE_PRODUCT_FULL_REDUCE = 2      // 商品满减券
    const val COUPON_TYPE_PRODUCT_DIRECT_DISCOUNT = 3  // 商品统一折扣券
    const val COUPON_TYPE_PRODUCT_DIRECT_REDUCE = 4    // 商品直减券
    const val COUPON_TYPE_PRODUCT_TRADEIN = 5          // 商品换购券
    const val COUPON_TYPE_PRODUCT_BUYGET = 6           // 商品买赠券
    const val COUPON_TYPE_SHOP_DISCOUNT = 101          // 店铺条件折扣券
    const val COUPON_TYPE_SHOP_FULL_REDUCE = 102       // 店铺满减券
    const val COUPON_TYPE_SHOP_DIRECT_DISCOUNT = 103   // 店铺统一折扣券
    const val COUPON_TYPE_SHOP_DIRECT_REDUCE = 104     // 店铺直减券
}

/** 微信直购小店优惠券信息。 */
interface IWxShopCouponInfo : IKmmKeep {
    val couponName: String
    val stockId: String
    val needReceive: Int
    val couponDetail: IWxShopCouponDetail?
    val promoteType: Int
}

/** 微信直购小店优惠券详情。 */
interface IWxShopCouponDetail : IKmmKeep {
    val couponKey: IWxShopCouponKey?
    val couponValue: IWxShopCouponValue?
}

/** 微信直购小店优惠券类型信息。 */
interface IWxShopCouponKey : IKmmKeep {
    val type: Int // @WxShopCouponType 优惠券类型
}

/** 微信直购小店优惠券优惠值信息。 */
interface IWxShopCouponValue : IKmmKeep {
    val discountInfo: IWxShopDiscountInfo?
}

/** 微信直购小店优惠信息。 */
interface IWxShopDiscountInfo : IKmmKeep {
    val discountCondition: IWxShopDiscountCondition?
    val discountNum: Int
    val discountFee: Long
}

/** 微信直购小店优惠条件。 */
interface IWxShopDiscountCondition : IKmmKeep {
    val productCnt: Int
    val productPrice: Int
}

/** 微信直购小店外显价格信息。 */
interface IWxShopShowPriceInfo : IKmmKeep {
    val estimateActivityId: Long
}

/** 微信直购小店补贴标签图片信息。 */
interface IWxShopSubsidyLabelImg : IKmmKeep {
    val stateSubsidyLabelImg: IWxShopImgInfo?
    val productSubsidyLabelImg: IWxShopImgInfo?
}

/** 微信直购小店图片信息。 */
interface IWxShopImgInfo : IKmmKeep {
    val imgUrl: String
    val imgWidth: Int
    val imgHeight: Int
    val imgType: Int
}
