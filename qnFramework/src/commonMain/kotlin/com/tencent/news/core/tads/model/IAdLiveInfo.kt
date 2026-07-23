package com.tencent.news.core.tads.model


interface IAdLiveInfo {

    // 直播播放需要这俩id
    val pid: String
    val streamId: String

    val playingStatus: Int      // 直播状态
    val playCount: Int          // 播放数
    val livePageScheme: String  // 直播跳转的落地页scheme

    val isValidLiveVideo: Boolean   // 是否是有效的直播视频
    val showPlayCnt: Boolean        // 是否展示播放数
    val rawPlayCnt: Int             // 原始播放数

}