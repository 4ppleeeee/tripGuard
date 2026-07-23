package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable


// 算法与上报透传相关
interface ITraceDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    var algVersion: String
    var seqNo: String
    var recommendReason: String
    var expid: String?
    var bucketId: String?
}