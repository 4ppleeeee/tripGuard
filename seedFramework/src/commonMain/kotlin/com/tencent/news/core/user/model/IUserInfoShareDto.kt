package com.tencent.news.core.user.model

import com.tencent.news.core.parcel.IKmmParcelable


// 用户分享信息
interface IUserInfoShareDto : IKmmParcelable {

    var shareTitle: String      // 分享标题
    var shareContent: String    // 分享摘要
    var shareImg: String        // 分享图
    var shareUrl: String        // 分享链接

}