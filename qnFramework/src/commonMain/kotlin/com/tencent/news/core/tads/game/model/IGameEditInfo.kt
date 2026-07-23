package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep


// 游戏精编信息

interface IGameEditInfo : IKmmKeep, IGamePageTypeHolder {

    val editId: String
    val editPic: String
    val editTitle: String
    val editIntro: String
    val editUrl: String

    val gameInfo: IGameInfo?

    val showCloudGameBtn: Boolean

    val isPickColor: Boolean

    fun isMiniGame(): Boolean

}