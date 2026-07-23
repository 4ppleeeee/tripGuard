package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IVideoInfoLiveDto : IKmmParcelable {

    var pid: String

    var sportsMatchId: String   // 体育直播比赛id

    var streamId: String        // 大直播流id
    var roomId: Long            // 直播房间id
    var playbackVid: String     // 直播回放vid
    var startTime: Long         // 直播开始时间

    var status: Int             // 直播状态 @LiveRoomStatus
    var liveType: Int           // 直播类型 @LiveType
    var isOrder: Int            // 直播预约状态

}