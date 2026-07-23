package com.tencent.news.core.share.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.parcel.IKmmParcelable


interface IItemShareDto : IItemDtoDoc, IShareDto, IKmmParcelable {

    var disableShare: Int

    var shareUrl: String

    var shareTitle: String

    var shareContent: String

    var shareImg: String

    var shareCount: String

    var pdfUrl: String

    var miniProShareUrl: String // 小程序的链接
    var miniProShareImage: String
    var miniProShareCode: String
    var textShareType: String

}