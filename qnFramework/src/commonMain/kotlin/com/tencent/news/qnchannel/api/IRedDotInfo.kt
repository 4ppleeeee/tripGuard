package com.tencent.news.qnchannel.api

import com.tencent.news.core.extension.IKmmKeep


interface IRedDotInfo : IKmmKeep {
    /**
     * 红点生效的开始时间，单位：秒
     */
    val startTime: Long

    /**
     * 红点生效的结束时间，单位：秒
     */
    val endTime: Long

    /**
     * 红点可消费的次数（注意，小红点必须点掉才算消费；气泡、动画等播放一次即算消费一次）
     */
    val showingSpan: Int

    /**
     * 红点出现的频次间隔：距离上次消费红点，要超过[repeatTimes]秒以上，才能再次显示
     */
    val repeatTimes: Int

    @get:RedDotType
    val dotType: Int

    /**
     * [RedDotType.BUBBLE] 类型，气泡显示的文案
     */
    val dotWord: String?

    /**
     * [RedDotType.ANIM] 类型，动画使用的lottie
     */
    val dotIcon: IIconStyle?

    /**
     * 红点编号，可用于去重
     */
    val dotNumber: String?
}