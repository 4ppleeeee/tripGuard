package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.IKmmKeep

/**
 * 评论中划线信息
 */
interface ICommentUnderLine : IKmmKeep {
    var underlineId: String // 划词ID
    var content: String // 划词内容
}