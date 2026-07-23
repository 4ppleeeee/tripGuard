package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep


// 游戏礼包信息

interface IGameGiftInfo : IKmmKeep {
    val giftPics: List<String>?
}