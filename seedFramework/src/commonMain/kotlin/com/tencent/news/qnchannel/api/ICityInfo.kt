package com.tencent.news.qnchannel.api

import com.tencent.news.core.extension.IKmmKeep


/**
 * 地方站频道相关信息（在 [IChannelInfo] 的基础上剥离出来的，为了保持接口精简）
 */

interface ICityInfo : IKmmKeep {
    /**
     * 频道设置页，右上角角标
     */
    val label: String?

    @get:ChannelType
    val channelType: Int

    /**
     * 城市所属的省份
     */
    val chanelGroup: String?

    /**
     * 城市的行政区码
     */
    val adCode: Int

    /**
     * 地方站推荐时使用的位置
     */
    val order: Int
}