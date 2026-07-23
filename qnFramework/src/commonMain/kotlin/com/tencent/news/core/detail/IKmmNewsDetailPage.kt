package com.tencent.news.core.detail

import com.tencent.news.core.list.model.IKmmFeedsItem

/**
 * 标记接口，用于标记图文底层页
 */
interface IKmmNewsDetailPage {
    fun getItemData(): IKmmFeedsItem?
}
