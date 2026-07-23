package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播基础信息 DTO 接口（跨平台暴露）
 */
interface IKmmNewsRoomInfoBaseDto : IKmmKeep, IKmmParcelable {
    var cmsId: String
    var liveType: Int
    var isMatch: Int
    var matchType: Int
    var isPay: Int
    var systemTime: Long
    var sharePage: String
    var shareImage: String
    var chatTips: String
    var liveSource: Int
    var entityType: String
}
