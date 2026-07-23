package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable


interface IModuleDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    var newsModule: IKmmNewsModule?

}