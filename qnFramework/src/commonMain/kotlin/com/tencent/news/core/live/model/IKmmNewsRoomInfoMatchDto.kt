package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 比赛直播信息 DTO 接口（跨平台暴露）
 */
interface IKmmNewsRoomInfoMatchDto : IKmmKeep, IKmmParcelable {
    var mid: String
    var competitionId: String
    var desc: String
    var startTime: String
    var endTime: String
    var matchStatus: Int
    var matchSource: Int
    var sportLiveType: Int
    var liveStatus: Int
    var seasonId: String
    var quarter: String
    var quarterTime: String
    var leftId: String
    var leftName: String
    var leftUrl: String
    var leftGoal: Int
    var rightId: String
    var rightName: String
    var rightUrl: String
    var rightGoal: Int
    var roundName: String
    var roundNumber: String
    var interveneHeadUrl: Int
    var activityTitle: String
}

