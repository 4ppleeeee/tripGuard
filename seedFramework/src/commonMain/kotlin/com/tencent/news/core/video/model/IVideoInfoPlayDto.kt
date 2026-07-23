package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IVideoInfoPlayDto : IKmmParcelable {

    var playUrl: String
    var playCount: Long
    var playDuration: String
    var aiChapterSummary: String?
    var aiChapter: List<AiChapter>?

    var watchedDuration: Int    // 已观看进度，单位秒

    val playReason: String      // vip播放状态 @VidPlayReason
    val payStatus: String
    val vidPayResult: Int

    // 拍摄声明相关：
    var videoDeclareInfo: String    // 自主声明文案
    var videoDeclareId: Int          // 自主声明id
    var declares: List<VideoDeclareInfo>? // 声明列表
    var aigcMark: String

    // 长视频联合信息（点映礼、片头片尾、下载权限等）：
    val unionExtra: IVideoUnion?

}