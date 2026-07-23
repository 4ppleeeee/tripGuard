package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep


// 游戏回流礼包信息

interface IGameReflowInfo : IKmmKeep, IGamePageTypeHolder {
    val reflowTitle: String
    val reflowDesc: String
    val reflowJumpUrl: String
    val reflowBgImg: String
}