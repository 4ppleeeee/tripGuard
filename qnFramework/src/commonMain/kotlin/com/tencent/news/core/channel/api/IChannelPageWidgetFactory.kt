package com.tencent.news.core.channel.api

import com.tencent.news.core.page.model.StructPageWidget2
import com.tencent.news.qnchannel.api.IChannelInfo

interface IChannelPageWidgetFactory {

    // 这里有开关控制，后面全量了会和createNonNull合并到一起
    fun create(channelInfo: IChannelInfo?): StructPageWidget2?

    // 这个主要再给鸿蒙用，创建非空widget
    fun createNonNull(channelInfo: IChannelInfo): StructPageWidget2

    // 创建通用无限刷频道 widget（getQQNewsUnreadList），可用于业务独立页复用频道请求链路
    fun createUnreadList(channelInfo: IChannelInfo): StructPageWidget2

}
