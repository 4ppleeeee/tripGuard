package com.tencent.news.core.qa.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

interface IItemQADto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    var qaInfo: IKmmQAInfo?

    var questionId: String?
}