package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

interface ICommentBaseDto : IKmmKeep, IKmmParcelable {

    var commentId: String       // 评论id
    var rootId: String          // 根评论id
    var parentId: String?       // 被回复的评论的replyId

    var articleId: String       // 评论所在文章id
    var articleTitle: String    // 评论所在文章标题

    var replyId: String         // 回复id
    var replyNum: Int           // 总回复数（包含1、2级回复的总数）
    var replyContent: String?
    val highLighting: IHighLighting?
    var underLine: ICommentUnderLine? // 划词的评论相关信息

    // 现在只有在假写二级评论的时候，这个 size 才会 = 2，其他情况下，size 都是 1
    // 二级假写的时候 [0] = 被回复的一级， [1] = 二级假写
    var replyList: ArrayList<ArrayList<IComment>>? // 评论页列表中的某个评论下面的回复，一般是2条

}