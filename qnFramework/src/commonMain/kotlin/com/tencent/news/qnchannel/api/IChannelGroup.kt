package com.tencent.news.qnchannel.api

interface IChannelGroup {

    val version: String?

    val tabId: String?

    val groupId: String?

    val groupName: String?

    val groupIcon: IIconStyle?

    val functionBtn1: IIconStyle?

    val functionBtn2: IIconStyle?

    val jumpUrl: String?

    val channelList: List<IChannelInfo>?

    val subTabs: List<IChannelGroup>?

    val redDotInfo: IRedDotInfo?

    val minVersion: Int

    val extraStates: List<IExtraState>?

    val showTabText: String?

    val showUnselectTabText: String?

    val h5ShowType: Int

    val extInfo: Map<String, String>?

    companion object {
        const val PACKAGE = "com.tencent.news.qnchannel.api"
        const val INTENT_TAB_ID = "${PACKAGE}.tabId"
        const val LOCAL_CHANNEL_LIST = "${PACKAGE}.localChannelList"
        const val H5_SHOW_TYPE_INNER = 1 // 当前tab嵌入式展示h5
    }
}


interface IExtraState {
    fun getChannelId(): String

    @ExtraStateType
    fun getCityOutStand(): Int

    fun getCategoryName(): String
}

annotation class ExtraStateType {
    companion object {
        const val CITY_OUTSTAND = 1
        const val CITY_NORMAL = 0
    }
}

fun IChannelGroup?.getExtraState(channelId: String?): IExtraState? {
    return this?.extraStates?.firstOrNull { channelId == it.getChannelId() }
}

/**
 * 从ext_info中取出key对应的value (ext_info是大圣配置的自定义属性，为键值对集合)
 */
fun IChannelGroup?.getExtInfoWithKey(key: String): String? {
    return this?.extInfo?.get(key)
}