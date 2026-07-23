package com.tencent.news.core.tads.model

interface IAdClickHotArea {
    val heightRate: Float
    val marginBottomRate: Float
    val marginLeftRate: Float
    val marginRightRate: Float
    val enableClick: Boolean
    val noClickDuration: Int get() = 0  // 曝光后禁止点击时长(ms)，默认0

    val isHeightRateValid: Boolean      // 下发空字符串时为false，用于区别0值和未下发

    val isBottomRateValid: Boolean      // 下发空字符串时为false，用于区别0值和未下发

    val isLeftRateValid: Boolean        // 下发空字符串时为false，用于区别0值和未下发

    val isRightRateValid: Boolean       // 下发空字符串时为false，用于区别0值和未下发
}