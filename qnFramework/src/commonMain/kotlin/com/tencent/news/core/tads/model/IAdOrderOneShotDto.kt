package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc


interface IAdOrderOneShotDto : IAdOrderDtoDoc {

    val isOneShotStream: Boolean
    var oneShotType: Int

    // 闪屏视频尾帧图片
    var lastFrameImageFilePath: String
    var lastFrameImageUrl: String

    // 信息流破窗视频
    var brokenVideoFilePath: String
    var brokenVideoVid: String
    var brokenVideoUrl: String  // 本地用vid换链回来的

}
