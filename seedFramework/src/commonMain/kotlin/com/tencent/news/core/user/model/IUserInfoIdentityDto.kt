package com.tencent.news.core.user.model

import com.tencent.news.core.parcel.IKmmParcelable


// 用户身份信息
interface IUserInfoIdentityDto : IKmmParcelable {

    var medalCount: Int     // 获得的勋章数量
    var isSponsor: Boolean  // 是否是赞助者（加热）

}