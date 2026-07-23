package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.user.model.IUserInfo

interface IVideoInfoBaseDto : IKmmParcelable {

    var vid: String     // 【重要】视频唯一标识的id，用这个id去视频服务进行换链播放
    var cid: String
    var lid: String

    var columnIdCms: String     // 长视频-对应lid
    var syncCoverCms: String    // 长视频-对应cid

    var idStr: String           // 单个视频对应的视频底层页newsid
    var title: String           // 跟desc的区别是比desc更简洁
    var desc: String

    var screenType: Int         // 视频横竖屏的标识，0：横屏，1：竖屏全屏 -1: 未知，说明后台没有下发
    var videoSourceType: Int    // 1是普通视频，2是直播视频

    val hotCount: Long          // IP底层-热度

    val style: String           // 图文底层页用，CMS下发的视频样式

    val isWeishiVideo: Boolean

    var card: IUserInfo?        // cp信息

}