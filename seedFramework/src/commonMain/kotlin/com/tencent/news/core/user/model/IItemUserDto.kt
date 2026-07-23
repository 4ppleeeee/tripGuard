package com.tencent.news.core.user.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.list.model.IItemUserInteractionDto
import com.tencent.news.core.parcel.IKmmParcelable


interface IItemUserDto : IItemDtoDoc, IUserDto, IKmmParcelable {

    override var card: IUserInfo?

    override var userInfo: IUserInfo?

    // todo fitzwu opt: Item里有个 IInteractionInfo 目前在 baseDto 里，这俩需要合并
    val interactionInfo: IItemUserInteractionDto

    @Deprecated("用 eventDto")
    var focusId: String

    var disableDeclare: String
    var disableDelete: Int
    var disableRepostTab: String // 文章是否不显示转发TAB
    var joinNum: Int // 评论专题头部讨论数量
    var followNum: Int     // 关注数量
    var joinUser: MutableList<IUserInfo>? // 互动用户头像列表（@cell618 / 热点精选picShowType=89）

}

fun IItemUserDto.getValidUserInfo(): IUserInfo? = card ?: userInfo