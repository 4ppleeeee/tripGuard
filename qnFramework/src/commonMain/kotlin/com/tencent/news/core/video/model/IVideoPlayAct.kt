package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IVideoPlayAct : IKmmParcelable {
    val title: String?  // 聚合标题
    val poster: String? // 聚合主图片
    val cornerTagType: Int
}
