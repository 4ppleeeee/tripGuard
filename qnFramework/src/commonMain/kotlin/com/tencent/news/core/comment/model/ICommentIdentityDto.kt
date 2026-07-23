package com.tencent.news.core.comment.model

import com.tencent.news.core.parcel.IKmmParcelable

interface ICommentIdentityDto : IKmmParcelable {

    var vipType: Int            // @VipType
    var vipTypeNew: Int         // 主要用于做头像旁边的vip icon资源映射使用
    var vipDesc: String
    var vipIcon: String
    var vipIconNight: String
    var vipPlace: String        // left/right，默认是left
    var isSponsor: Boolean       // 是否有加热标签

}