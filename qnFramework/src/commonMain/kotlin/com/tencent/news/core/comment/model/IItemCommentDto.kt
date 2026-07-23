package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable


interface IItemCommentDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {
    var firstComment: IComment?
    var allComments: List<IComment>?
    var commentItem: List<IComment>?
    var commentId: String
    var commentFrom: String?

    var comments: Long
    var voteId: String

    var commentSyncWeibo: String // 标识这篇文章是否支持评论生成动态
    var voteInfo: String // 后台下发的投票原始数据，会解析成 voteInfoObject

}