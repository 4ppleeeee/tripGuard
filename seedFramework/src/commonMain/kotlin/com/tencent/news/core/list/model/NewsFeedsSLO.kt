package com.tencent.news.core.list.model

import com.tencent.news.core.getPlatform
import com.tencent.news.core.list.trace.NewsChannelLog


object NewsFeedsSLO {

    inline fun debugLog(msg: () -> String) {
        NewsChannelLog.debug("SLO") { "${logPrefix()}：${msg()}" }
    }

    fun mainLog(msg: String) {
        NewsChannelLog.fileLog("SLO", "${logPrefix()}：${msg}")
    }

    fun logPrefix() = "【SLO耗时】- 线程${getPlatform().currentThreadName()}"

}