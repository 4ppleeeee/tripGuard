package com.tencent.news.core.list.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IHotEventBaseDto : IKmmParcelable {

    var idStr: String   // 最早原意为事件id（32位长字符串，非UTR开头），目前后台下发的idStr和cmsId基本一致了
    var cmsId: String   // 事件发文的cmsId（一般都是 UTR 开头）

    var title: String
    var image: String   // 事件图标

    var businessType: String    // 类型：事件、问答、IP 等等 @StructPageBusinessType
    var businessName: String

    val manualAbstract: String  // 运营手填的摘要

    val eventLineId: String     // 事件脉络id

    val aggregationOuterIcon: String?   // 竖版视频底部挂件外显素材
    val aggregationOuterBgBar: String?

    val focusArticleType: String? // 焦点文章类型

    // 接入层控制逻辑：统一播放目标
    val playId: String?
    val playArticleType: String?

}