package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

// 信息流Item互动模块
interface IItemUserInteractionDto : IKmmKeep, IKmmParcelable {

    var likeCount: String       // 点赞数
    var interaction: Int        // 微博是否可交互
    var forbidExpr: String      // 禁止评论页表态

    @Deprecated("这个迁移到 videoDto 里了")
    val openSupport: Boolean    // 视频是否可点赞

    var enableDiffusion: String // 链接型文章是否能够点赞 微信公众号文章等

    var emojiSwitch: Int        // 表情输入开关：0关，1开

    var collectCount: Int       // 收藏数

}