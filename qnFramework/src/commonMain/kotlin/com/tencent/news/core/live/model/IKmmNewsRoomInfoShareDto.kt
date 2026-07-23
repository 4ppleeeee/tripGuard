package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播分享信息 DTO 接口（跨平台暴露）
 */
interface IKmmNewsRoomInfoShareDto : IKmmKeep, IKmmParcelable {
    var shareTitle: String
    var shareImage: String
    var shareDesc: String
    var allStreamsShareTitle: Map<String, String>
}

