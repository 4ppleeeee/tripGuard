package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播播放信息 DTO 接口（跨平台暴露）
 */
interface IKmmNewsRoomInfoPlayDto : IKmmKeep, IKmmParcelable {
    var width: Int
    var height: Int
    var hvDirection: Int
    var clientType: Int
    var appid: Int
    var beginTimestamp: String
    var playClientType: Int
    var mode: String
    var streamInfo: INewsLiveStreamInfo?
    var playbackVid: String
    var playbackCid: String
    var yspLivingUrl: String
    var yspLivingSchema: String
    var yspPlaybackUrl: String
    var yspPlaybackSchema: String
    var yspPid: String
    var yspVid: String
    var previewVid: String
    var streamId: String
    var multiStream: List<INewsLiveMultiStreamItem>
}

