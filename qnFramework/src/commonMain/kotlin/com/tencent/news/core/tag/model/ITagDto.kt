package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.RelateTagInfo
import com.tencent.news.core.parcel.IKmmParcelable


interface ITagDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    var tagInfo: IKmmTagInfo?

    var tagInfoItemFull: TagInfoItemFull?

    var relateTagInfoList: MutableList<RelateTagInfo>?

}