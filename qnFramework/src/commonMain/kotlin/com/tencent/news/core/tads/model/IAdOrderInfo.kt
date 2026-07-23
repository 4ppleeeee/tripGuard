package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc
import com.tencent.news.core.list.api.IExportModelData


interface IAdOrderInfo : IAdOrderDtoDoc, IExportModelData {

    var oid: String         // 订单号
    var soid: String        // 【极其重要】：数据流程中重要的key，一般具有唯一性
    var cid: String         // 创意id
    var uoid: String        // 频道间信息流订单兼容OID碰撞，信息流优先使用uoid作为key （可能已废弃）
    val uniqueId: String    // 作为插入列表的唯一标识（同一订单处于不同位置id也不相同），AdOrderInfo中作为判断逻辑使用
    val netLogId: String    // 网络日志id（机器猫鹰眼可查）

    var subType: Int        // 订单样式 @AdSubType
    var actType: Int        // 订单跳转类型 @AdActType
    var destType: Int       // 落地页跳转类型
    val orderType: Int      // 广告类型 @AdOrderType


    var advertiserName: String  // 广告主名称
    var advertiserIcon: String  // 广告主头像
    var advertiserId: Long      // 广告主id，上报用

    var advertiserClickNum: Long // 广告订单点击量

    var productId: Long         // 获取产品id，上报用
    var productType: Int        // 获取产品类型，上报用

    var title: String           // 订单大标题
    var shortTitle: String      // 短标题
    var longTitle: String       // 订单长标题（目前没实际意义了，和title一样）
    var abstract: String        // 简要（用于展示二级标题的时候用）
    var gameScore: String       // 游戏评分
    var giftTotalNum: String    // 礼包数量
    var gameAppId: String?      // 游戏 App ID
    var gamePackageId: String?  // 游戏包名

    var shareTitle: String      // 分享标题
    var shareUrl: String

    // 一级行业
    var adFirstCategory: Int

    // 获取广告行业id，上报用
    var industryId: Long

    // 获取后台下发新鲜度，上报用
    val adContext: String

    // 广告分类id：与图文文章的 FartForCatalog 字段相匹配
    var columnId: String

    // 透传car policy返回的AdInfo中的数据，数据为json字符串
    val amsAdInfo: String

    // 广告位相关信息（类似loid但取值不一样，这个是ams内部的值，一般用于透传）
    val posInfo: AdPosInfo?

    // 广告级别的traceId，每个广告单唯一
    var traceId: String

    var viewId: String // conv_view_id（目前链路上报会用）

    // 刷次级别的traceId，一刷里的订单都是同一个值（后台会在每个订单里也下发一个）
    val amsTraceId: String

    // 标签
    val labels: List<IAdLabel>

    // 原生行业外显标签
    val nativeLabels: List<IAdLabel>

    // 行业样式判断
    val nativeStyleType: String

    val wxGameStyleInfo: IWxGameStyleInfo?

    val jdtFrame: Int           // seq相同时的广告序号（数字大的放后面）
    var orderClass: Int         // 广告订单类型
    val horLiveSubTitle: String // 直播广告副标题（仅横版）

    val conversionInfo: IAdConversionInfo? // 信息流外显信息

    val olympicPushAdInfo: String

    // 负反馈隐藏行业类开关
    var feedbackHideIndustry: Boolean

    // 二级行业名称
    val secondIndustryName: String

    // 广告是否内容上下文相关
    var isContextuallyRelevant: Boolean

    val weChatStoreInfo: IAdWeChatStoreInfo?  // 微信小店直购信息

    val couponList: List<IAdCouponInfo>?      // 电商通用-优惠券列表

    val floatingZoneInfo: IAdFloatingZoneInfo? // 兜底大卡浮层信息

    val playLetInfo: IAdPlayLetInfo?  // 短剧信息

    val articleType: String         // 文章类型（使用新闻底层页打开广告落地页时使用）.

    var dspName: String             // DSP订单来源.

    val playableMiniGameInfo: IAdPlayableMiniGameInfo?

    val commentSum: String          // 落地页可评论广告的评论数

    var commentId: String           // 落地页可评论广告的评论ID

    val localInfo: IAdLocalInfo?

    val realtimeLocation: String

    val countDownInfo: IAdCountDownInfo?  // 倒计时信息

    val timelineWidget: IAdTimelineWidget?  // 时间轴广告


}
