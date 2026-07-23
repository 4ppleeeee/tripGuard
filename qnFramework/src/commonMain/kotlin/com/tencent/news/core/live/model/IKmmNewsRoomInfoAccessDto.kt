package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播权限控制信息 DTO 接口（跨平台暴露）
 * 使用驼峰命名，便于各平台使用
 */
interface IKmmNewsRoomInfoAccessDto : IKmmKeep, IKmmParcelable {
    
    var isPresident: Int
    var disableLike: Int
    var disableChat: Int
    var disableShare: Int
    var disableAd: Int
    var disablePopularity: Int
    var disableRecommendLive: Int
    var disableEmotionBarrage: Int
    var disableLiveActivity: Int
    var disablePcAd: Int
    var disableSdkPublishBarrage: Int // 禁止sdk发弹幕，1 是，0 否
    var disableFloatBarrage: Int
    var disableReportBarrage: Int // 禁止弹幕举报
    var disableLocationCheckIn: Int // 0允许定位打卡，1不允许定位打卡
    var disableRealTimePlayback: Int // 是否允许直播回看 0 允许，1 不允许
    var disableMultiScreen: Int // 是否允许多屏 0 允许，1 不允许
    var disableCaption: Int // 禁用AI字幕
    var disableLikeBarrage: Int // 是否禁用弹幕点赞
    var disableShowRecommendLive: Int // 是否禁用简介更多视频推荐
    var disablePosterShare: Int // 是否禁用海报分享
    var disableEffectGift: Int // 是否禁用礼物
    var disableSummary: Int // 是否禁用智能摘要
    var disableGift: Int // 是否禁用付费礼物
    var disableFocus: Int
    var disablePullApp: Int
    var enablePk: Int // pk 入口，0 关 1 开
    var disableDigitalConnection: Int // 是否禁用数字人
}