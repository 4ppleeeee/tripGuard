package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播房间信息 DTO 接口（跨平台暴露）
 */
interface IKmmNewsRoomInfoRoomDto : IKmmKeep, IKmmParcelable {
    var roomId: String
    var roomTitle: String
    var picLiveTitle: String
    var icons: NewsLiveRoomIcons?
    var viewer: String
    var popularity: String
    var roomState: Int
    var labelList: List<NewsLiveRoomLabel>
    var programId: String
    var businessPayFlag: Int
    var businessPid: String
    var watchKey: String
    var subTitle: String
    var describe: String
    var startTime: String
    var endTime: String
    var tabs: List<NewsLiveRoomTab>
    var likeCount: Long
    var likeIconLight: String
    var likeIconDark: String
    var floatingAnimationIos: String
    var floatingAnimationAndroid: String
    var pendant: List<RoomPendantInfo>
    var vote: NewsLiveRoomVote?
    var isOrder: Int
    var backgroundPic: String
    var agentPendant: NewsLiveAgentPendant?
    var liveTheme: List<NewsLiveThemeAd>?
}


