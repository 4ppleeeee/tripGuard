package com.tencent.news.core.comment.constants

enum class CommentContentType(val reportValue: String) { // 值不要随便改，上报会用
    ORIGIN("origin"),               // 原创评论
    FIRST_REPLY("firstReply"),      // 一级回复
    SECOND_REPLY("secondReply"),    // 二级回复
}