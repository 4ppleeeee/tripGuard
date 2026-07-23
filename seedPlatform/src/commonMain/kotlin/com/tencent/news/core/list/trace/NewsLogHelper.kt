package com.tencent.news.core.list.trace

import com.tencent.news.core.extension.ILogDoc
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.platform.qnFileLog
import com.tencent.news.core.platform.qnLogcat

open class BaseBizLog(val tag: String, val subTag: String = "") : ILogDoc {

    inline fun verbose(subTag: String, msg: () -> String) {
        if (isDebug()) {
            qnLogcat()?.logV("$tag/$subTag", msg())
        }
    }

    // 比较耗时的拼接用这个
    inline fun debug(subTag: String = this.subTag, msg: () -> String) {
        if (isDebug()) {
            qnLogcat()?.logI("$tag/$subTag", msg()) // 不细分d和i级别了，最常用的都是这个i的
        }
    }

    inline fun warn(subTag: String = this.subTag, msg: () -> String) {
        if (isDebug()) {
            qnLogcat()?.logW("$tag/$subTag", msg())
        }
    }

    fun fileLog(msg: String) {
        fileLog(subTag, msg)
    }

    fun fileLog(subTag: String = this.subTag, msg: String) {
        qnFileLog()?.logW("$tag/$subTag", msg)
    }

    fun error(msg: String, error: Throwable? = null) {
        error(subTag, msg, error)
    }

    fun error(subTag: String = this.subTag, msg: String, error: Throwable? = null) {
        qnFileLog()?.logE("$tag/$subTag", msg, error)
    }

}
