package com.tencent.news.qnchannel.api


annotation class ChannelState {

    companion object {
        /**
         * 频道默认状态，可见
         */
        const val SHOW = 0

        /**
         * 频道隐藏，频道位置依然会保留在列表中，但对用会不可见；导航信息上报会包含隐藏的频道（普遍用于临时隐藏频道再恢复）
         */
        const val HIDE = 1

        /**
         * 频道下线，理论上接入层会做数据过滤，不再下发；客户端这里同样做一个保护处理
         */
        const val OFFLINE = 2

        /**
         * debug专用，仅在debug包可显示
         */
        const val DEBUG = 3
    }

}

interface IChannelState {
    @get:ChannelState
    val channelState: Int
}