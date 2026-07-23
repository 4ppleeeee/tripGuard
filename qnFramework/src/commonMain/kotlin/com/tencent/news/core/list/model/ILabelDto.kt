package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable


interface ILabelDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {
    var hideUpLabelList: Int

    var labelImage: ILabelImage?
}