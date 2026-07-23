package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IVideoBlackBorder : IKmmParcelable {
    val isBlackBorder: Boolean      // 是否有黑边
}
