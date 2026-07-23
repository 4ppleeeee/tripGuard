package com.tencent.news.core.detail

import com.tencent.news.core.list.model.IKmmFeedsItem

interface IKmmArticleProvider {
    /**
     * 底层页文章item
     */
    fun getItem(): IKmmFeedsItem?

    /**
     * 底层页所在的二级频道
     */
    fun getNewsChannel(): String?
}