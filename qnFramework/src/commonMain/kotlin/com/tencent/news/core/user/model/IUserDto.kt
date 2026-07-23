package com.tencent.news.core.user.model

import com.tencent.news.core.extension.IKmmKeep


interface IUserDto : IKmmKeep {

    var card: IUserInfo?
    var userInfo: IUserInfo?
}