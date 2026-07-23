package com.tencent.news.qnchannel.api


interface IChannelTabProvider {
    fun getTabList(): List<IChannelInfo>?
    fun getDefaultTab(): String?
}