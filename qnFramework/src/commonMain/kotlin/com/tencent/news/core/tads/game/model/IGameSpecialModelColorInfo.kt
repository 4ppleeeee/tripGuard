package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep

// 游戏专区模块颜色信息接口
interface IGameSpecialModelColorInfo : IKmmKeep {
    val primaryColor: String

    val primaryOpacity: Float

    val secondaryColor: String

    val secondaryOpacity: Float

    val backgroundColor: String

    val backgroundOpacity: Float
}