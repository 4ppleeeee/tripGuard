package com.tencent.news.core.compose.trace

import com.tencent.news.core.list.trace.ComposeViewLog

object ComposePageTrace {

    fun record(msg: String) {
        ComposeViewLog.debug("ComposePageTrace") { msg }
    }

}