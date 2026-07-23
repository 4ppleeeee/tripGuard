package com.tencent.news.core.comment.model


interface IHighLighting {
    val list: List<ISingleHighLighting>? // 划词列表
}

interface ISingleHighLighting {
    var startIndex: Int     // 划词在评论里的索引
    var endIndex: Int       // 划词在评论里的索引
    var word: String        // 划词内容
    var scheme: String      // 跳转的schema
    var huaciType: String   // 划词类型
}