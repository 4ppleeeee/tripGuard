package com.tencent.news.core.tag.model

import com.tencent.news.core.parcel.IKmmParcelable


interface ITagBaseDto : IKmmParcelable {

    var tagId: String
    var tagType: String
    var tagName: String
    var tagDesc: String     // 摘要信息
    var groupName: String   // 所属分类

    var tagScene: String    // tag类型
    var tagSceneName: String

    val timeDesc: String

    val subCount: Long          // 关注该tag的人数
    var collectCount: Long      // 收藏数
    var shareCount: Long        // 分享数

    val lastArticleId: String   // 最新的文章id（tag请求里携带的文章id参数 由接入层下发 透传回去）

    var columnContentType: String // 如果是article 图文专栏 如果是video 视频专栏
    val tagTotal: Int           // 合集内容总数
    val entityType: String

    val jumpScheme: String      // tag小条跳转scheme

}