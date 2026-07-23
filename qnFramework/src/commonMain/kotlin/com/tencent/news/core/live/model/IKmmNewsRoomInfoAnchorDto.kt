package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播主播信息 DTO 接口（跨平台暴露）
 */
interface IKmmNewsRoomInfoAnchorDto : IKmmKeep, IKmmParcelable {
    var liveUid: String
    var nickname: String
    var icon: String
    var bizUid: String
    var suid: String
    var mediaId: String
    var payingContentAccess: Int
    var vipInfo: INewsLiveVipInfo?
}

