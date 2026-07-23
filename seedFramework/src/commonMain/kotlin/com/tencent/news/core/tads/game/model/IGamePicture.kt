package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep


// 游戏用到的图片结构

interface IGamePicture : IKmmKeep, IGamePageTypeHolder {
    val picUrl: String
    val nightPicUrl: String
    val picIconUrl: String
    val clickUrl: String
    val picIntro: String
    val picName: String

    val imageId: String

    val shopPageDetail: IGamePictureShopPageDetail?
    val jumpType: Int

    val gameInfo: IGameInfo?
    val miniGameInfo: IMiniGameInfo?
}

interface IGamePictureShopPageDetail : IKmmKeep {
    val vid: String
    val videoUrl: String

    val hasVideo: Boolean
        get() = vid.isNotEmpty() || videoUrl.isNotEmpty()
}
