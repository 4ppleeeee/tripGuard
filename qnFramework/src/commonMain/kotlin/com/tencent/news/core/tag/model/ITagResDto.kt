package com.tencent.news.core.tag.model

import com.tencent.news.core.parcel.IKmmParcelable

interface ITagResDto : IKmmParcelable {

    var iconUrl: String
    var icon: String
    var bgImage: String     // 背景图

    val aggregationOuterIcon: String?   // 竖版视频底部挂件外显素材
    val aggregationOuterBgBar: String?

}