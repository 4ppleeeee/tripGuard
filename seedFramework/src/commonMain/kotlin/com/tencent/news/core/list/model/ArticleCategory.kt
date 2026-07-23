package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep

// 图文底层页，一二级分类
data class ArticleCategory(
    val firstCategory: Int,
    val secondCategory: Int
) : IKmmKeep