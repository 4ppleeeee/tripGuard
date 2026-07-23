package com.tencent.news.core.list.controller

import com.tencent.news.core.list.api.IStructDataRepo
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.list.trace.trimLogChannel
import com.tencent.news.core.page.model.StructPageConfig
import com.tencent.news.qnchannel.api.IChannelInfo

// 一些快捷工具类
internal object FlexCtrlCommonHelper {

    fun FlexCtrl.dataRepo(): IStructDataRepo = rootWidget.pageConfig.dataRepo

    fun FlexCtrl.pageConfig(): StructPageConfig = rootWidget.pageConfig

    fun FlexCtrl.getDefaultChannelInfo(): IChannelInfo = pageConfig().defaultChannelInfo

    inline fun FlexCtrl.debugLog(msg: () -> String) {
        NewsChannelLog.debug(getLogKey(), msg)
    }

    fun FlexCtrl.fileLog(msg: String) {
        NewsChannelLog.fileLog(getLogKey(), msg)
    }

    fun FlexCtrl.errorLog(msg: String, error: Throwable? = null) {
        NewsChannelLog.error(getLogKey(), msg, error)
    }

    fun FlexCtrl.getLogKey(): String {
        val channelInfo = rootWidget.pageConfig.defaultChannelInfo
        val newsChannel = channelInfo.env.newsChannel.trimLogChannel()
        val channelKey = channelInfo.channelKey.trimLogChannel()

        if (newsChannel != channelKey) {
            return "${newsChannel}/${channelKey}"
        }
        return channelKey
    }

}
