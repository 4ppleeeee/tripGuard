package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.IKmmKeep

interface ICommentInteractionInfo : IKmmKeep {

    var agreeCount: String      // 点赞数
    var pokeCount: String       // 点踩数

}