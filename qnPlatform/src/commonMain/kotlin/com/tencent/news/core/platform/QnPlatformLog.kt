@file:Suppress("unused")

package com.tencent.news.core.platform

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.platform.api.isRdm


/**
 * 由双端业务侧注入的log工具
 */

object QnPlatformLog : IPlatformInject {

    /**
     * IDE 输出的调试log，仅debug包使用
     */
    var logcat: ICommonLog? = null

    /**
     * 文件log，分享客户端日志时包含
     */
    var fileLog: ICommonLog? = null

}


interface ICommonLog {
    fun logV(tag: String, msg: String)
    fun logD(tag: String, msg: String)
    fun logI(tag: String, msg: String)
    fun logW(tag: String, msg: String)
    fun logE(tag: String, msg: String, throwable: Throwable? = null)
}


open class DefaultCommonLog : ICommonLog {
    override fun logV(tag: String, msg: String) {
        println("[${tag}] $msg")
    }

    override fun logD(tag: String, msg: String) {
        println("[${tag}] $msg")
    }

    override fun logI(tag: String, msg: String) {
        println("[${tag}] $msg")
    }

    override fun logW(tag: String, msg: String) {
        println("[${tag}] $msg")
    }

    override fun logE(tag: String, msg: String, throwable: Throwable?) {
        println("[${tag}] $msg $throwable")
    }
}

class EmptyLogcatImpl : ICommonLog {
    override fun logV(tag: String, msg: String) = Unit

    override fun logD(tag: String, msg: String) = Unit

    override fun logI(tag: String, msg: String) = Unit

    override fun logW(tag: String, msg: String) = Unit

    override fun logE(tag: String, msg: String, throwable: Throwable?) = Unit
}

// debug&rdm包正常打印日志，其他包不打印
@KmmInternalApi
fun qnLogcat(): ICommonLog? = if (isDebug() || isRdm()) QnPlatformLog.logcat else EmptyLogcatImpl()

@KmmInternalApi
fun qnFileLog(): ICommonLog? = QnPlatformLog.fileLog