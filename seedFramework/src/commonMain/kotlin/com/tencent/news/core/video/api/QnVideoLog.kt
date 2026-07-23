package com.tencent.news.core.video.api

import com.tencent.news.core.platform.qnLogcat

object QnVideoLog {
    private const val TAG = "QnVideoLog"

    fun log(msg: String) {
        qnLogcat()?.logI(TAG, msg)
    }
}
