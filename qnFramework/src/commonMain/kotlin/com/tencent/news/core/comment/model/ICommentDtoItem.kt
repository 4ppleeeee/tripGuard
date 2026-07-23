package com.tencent.news.core.comment.model

import com.tencent.news.core.extension.ICmsModelDtoItemDoc


interface ICommentDtoItem : ICmsModelDtoItemDoc {
    val baseDto: ICommentBaseDto            // 评论基础信息
    val userDto: ICommentUserDto            // 评论用户相关信息
    val identityDto: ICommentIdentityDto    // 用户身份信息：vip等
    val shareDto: ICommentShareDto          // 评论分享信息
    val commentCtxDto: ICommentContextDto   // 客户端本地绑定数据
    val extDto: ICommentExtDto              // 扩展字段
}