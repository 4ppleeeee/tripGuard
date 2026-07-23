package com.tencent.news.core.tag.model

import com.tencent.news.core.parcel.IKmmParcelable


interface ITagInfoColumnDto : IKmmParcelable {

    var columnAttr: ITagColumnAttr?    // 专栏付费相关信息
    var tagMediaInfo: ITagMediaInfo?  // 专栏新增专栏自己的作者信息

}