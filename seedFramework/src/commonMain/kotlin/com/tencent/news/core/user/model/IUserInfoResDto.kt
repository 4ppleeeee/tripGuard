package com.tencent.news.core.user.model

import com.tencent.news.core.parcel.IKmmParcelable


// 用户素材资源
interface IUserInfoResDto : IKmmParcelable {

    var headUrl: String             // 用户头像url

    var themeBackgroundUrl: String  // Header背景图url，结构化卡片使用，宽高都是375dp，cp和普通用户统一取这个字段

}