package com.tencent.news.core.user.model

import com.tencent.news.core.extension.ICmsModelDtoItemDoc


interface IUserInfoDtoItem : ICmsModelDtoItemDoc {
    val baseDto: IUserInfoBaseDto           // 用户基础信息
    val ugcDto: IUserInfoUgcDto             // ugc数据：关注、评论、赞 等等
    val resDto: IUserInfoResDto             // 用户素材资源：头像等
    val shareDto: IUserInfoShareDto         // 分享信息
    val identityDto: IUserInfoIdentityDto   // 用户身份信息：vip等
    val userCtxDto: IUserInfoContextDto     // 客户端本地绑定数据
}