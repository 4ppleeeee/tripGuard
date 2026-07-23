package com.tencent.news.core.list.model

import com.tencent.news.core.extension.safeToLong
import com.tencent.news.core.parcel.IKmmParcelable

interface IHotEventUgcDto : IKmmParcelable {

    var hotScore: Long          // 热问值
    var hotScoreStr: String     // 目前宿主老逻辑用str的多，包装一个方法
        get() = hotScore.toString()
        set(value) {
            hotScore = value.safeToLong()
        }

    var ranking: Int            // 热点榜第${ranking}名
    var readCount: Long         // 阅读量

    var recIcon: String         // 推荐标签
    var nightRecIcon: String

}