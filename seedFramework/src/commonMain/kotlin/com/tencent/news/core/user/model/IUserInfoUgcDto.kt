package com.tencent.news.core.user.model

import com.tencent.news.core.parcel.IKmmParcelable


// 用户的UGC互动数据
interface IUserInfoUgcDto : IKmmParcelable {

    val isMyFollow: Boolean // 这个用户是否是当前登录的用户关注的人

    var followCount: Int    // 关注数

    @Deprecated("use fansCount instead")
    var subCount: Int      // 粉丝数

    @Deprecated("use fansCount instead")
    var fansNum: Int      // 粉丝数

    var fansCount: Int      // 粉丝数

}