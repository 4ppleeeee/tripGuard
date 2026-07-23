package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 文章包含的AI推荐相关的数据
 */
interface IAiDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {
    // / 聚合了哪些文章
    val relateExtendInfos: QnKmmFeedsItemList
}