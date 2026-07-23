package com.tencent.news.core.comment.model

import com.tencent.news.core.comment.constants.CommentContentType
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

interface ICommentExtDto : IKmmKeep, IKmmParcelable {
    val links: List<ICommentLinkItem>?

    // 客户端绑定字段：
    var commentContentType: CommentContentType? // 评论内容类型
    var baseReplyId: String?                    // 主评论id，用在回复评论中
    var requestNum: Int                         // 点击展开请求次数
    var requestMoreParams: CommentMoreParams?   // 分页请求参数

    var parentComment: IComment?                // 根评论

    var virtualReplyNum: Int        // 本地假评论
    var refreshReplyNum: Int        // 刷新后评论回复数
    var netReplyDelNum: Int         // 本地删除评论（不含假评论）

}