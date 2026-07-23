package com.tencent.news.core.list.trace

import com.tencent.news.core.list.model.IKmmFeedsItem


fun IKmmFeedsItem?.getLogStr(): String {
    this ?: return "null"

    return "${ctxDto.newsChannel} " +
            "ID:${baseDto.idStr}, " +
            "类型:${baseDto.articleType}, " +
            "样式:${baseDto.picShowType}, " +
            baseDto.title.take(16)
}