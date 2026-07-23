package com.tencent.news.qnchannel.api

import kotlin.jvm.JvmStatic


annotation class ChannelType {

    companion object {
        const val NORMAL = 0

        const val CITY = 1

        const val PROVINCE = 2
    }

    object Helper {
        @JvmStatic
        fun isCityChannel(info: IChannelInfo?): Boolean {
            val channelType: Int = info?.city?.channelType
                ?: return false
            return CITY == channelType || PROVINCE == channelType
        }
    }

}
