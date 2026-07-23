package com.tencent.news.core.tads.model.interact

import com.tencent.news.core.extension.IKmmKeep

// 游戏专区礼包列表接口
interface IGameSpecialGiftList : IKmmKeep {
    val respId: String
    val retCode: Int
    val gameSpecialGifts: IGameSpecialGift?
}

interface IGameSpecialActivityInfos : IKmmKeep {
    val activityId: Int
    val safeId: String
    val gameSpecialGiftInfos: List<IGameSpecialGiftInfo>?
}

interface IGameSpecialGift : IKmmKeep {
    val gameSpecialGiftInfos: List<IGameSpecialGiftInfo>?
    val gameSpecialSignInNum: IGameSpecialGiftDayInfo?
}

interface IGameSpecialGiftInfo : IKmmKeep {
    val awardName: String
    val awardUrl: String
}

interface IGameSpecialGiftDayInfo : IKmmKeep {
    val alreadySignInNum: Int
    val todaySignIn: Boolean
}