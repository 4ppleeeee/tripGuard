package com.tencent.news.core.live.model

import com.tencent.news.core.extension.ICmsModelDtoItemDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播 DTO 接口
 * 用于在 Item 中管理直播相关信息
 * 跨平台数据模型
 */
interface ILiveDto : ICmsModelDtoItemDoc, IKmmKeep, IKmmParcelable {
    
    /**
     * 直播信息
     */
    var liveInfo: KmmLiveInfo?
    
    /**
     * 图文直播ID
     */
    var graphicLiveID: String

    var newsLiveInfo: IKmmNewsRoomInfoData?

    var roseLiveStatus: String // 直播状态：空是不需要展示;1未开始,2进行中,3已结束,4直播回放
    var zhiboVid: String // 直播视频ID
    
    var isLive: String // 是否直播
    var isPay: Int // 1表示付费直播
    var descWording: String // 视频推荐页卡-直播cell的附属信息
}