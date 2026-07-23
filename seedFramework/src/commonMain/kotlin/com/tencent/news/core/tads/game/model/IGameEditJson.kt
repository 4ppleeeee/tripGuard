package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep


// 游戏精编信息——额外数据透传json放这里（例如从飞鹰拉取的合规数据）

interface IGameEditJson : IKmmKeep {

    val pcGameImg: String       // 礼包推广图
    val simpleIntro: String     // 游戏简洁推荐语
    val bannerImg: String
    val headImg: String
    val welfarePromotionImg: String
}