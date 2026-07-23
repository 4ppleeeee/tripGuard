package com.tencent.news.core.comment.model

import com.tencent.news.core.parcel.IKmmParcelable

interface ICommentShareDto : IKmmParcelable {
    var shareUrl: String
    var sharePic: String
    var shareMainTitle: String
    var shareSubTitle: String
}