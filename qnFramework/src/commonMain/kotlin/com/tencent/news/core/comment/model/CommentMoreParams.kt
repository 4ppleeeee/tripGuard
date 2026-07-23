package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.IKmmKeep
import kotlin.math.max

/**
 *  评论拉取 更多回复 的请求参数
 */
class CommentMoreParams(
    val articleId: String = "",     // 父评论的文章id
    val commentId: String = "",     // 父评论的评论id
    val origId: String = "",        // 父评论的文章replyId

    var coralScore: String = "",    // 上次分页最后一篇回复的 coral_score
    var transparam: String = "",    // 新版本分页标识

    // 产品要求：2级评论数>5条，则首次展开5条，第二次再展开10条，第3次再展开20条，第4次全部展开
    var requestNum: Int = 5         // 下一次请求分页时的 requestNum
) : IKmmKeep {

    val originCoralScore = coralScore   // 原始的请求信息，用于 收起 后重置参数

    val moreReplyIdList = mutableListOf<String>() // 展开请求拉到的评论数据，只存 replyId

    fun updateRequestNum() {
        requestNum = max(requestNum * 2, 20)
    }

    fun reset() {
        transparam = ""
        requestNum = 5
        coralScore = originCoralScore
        moreReplyIdList.clear()
    }

}