package com.tencent.news.core.comment.model

import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.user.model.IUserInfo

interface ICommentUserDto : IKmmParcelable {

    var cmtUser: IUserInfo?

    val interactionInfo: ICommentInteractionInfo

    var suid: String?

    var nick: String
    var headUrl: String?

    var coralScore: String?  // 回答的评分

}