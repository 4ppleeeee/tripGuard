package com.tencent.news.core.video.model

import com.tencent.news.core.extension.ICmsModelDtoItemDoc

interface IVideoInfoDtoItem : ICmsModelDtoItemDoc {
    val baseDto: IVideoInfoBaseDto      // 基础信息
    val resDto: IVideoInfoResDto        // 素材资源
    val playDto: IVideoInfoPlayDto      // 播放信息
    val liveDto: IVideoInfoLiveDto      // 直播信息
    val adDto: IVideoInfoAdDto          // 广告信息
    val videoCtxDto: IVideoInfoContextDto   // 本地绑定参数
}