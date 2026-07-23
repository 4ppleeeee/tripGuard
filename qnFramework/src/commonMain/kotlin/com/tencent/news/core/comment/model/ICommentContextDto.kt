package com.tencent.news.core.comment.model

import com.tencent.news.core.parcel.IKmmParcelable

interface ICommentContextDto : IKmmParcelable {

    var isUnderlineCardOpen: Boolean

    /**
     * 评论标题是否显示评论数
     */
    var canShowCommentCount: Boolean
}