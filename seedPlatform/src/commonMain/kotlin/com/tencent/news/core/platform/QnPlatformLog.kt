@file:Suppress("unused")

package com.tencent.news.core.platform

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

    /**
     * 视频相关日志（独立文件夹落盘），用于播放器等视频链路。
     * - 未注入时，`qnVideoFileLog()` 会回退到 [fileLog]，保持向下兼容。
     */
    var videoFileLog: ICommonLog? = null

    /**
     * Compose 重组分析日志（独立文件夹落盘），用于 RecompositionProfiler。
     * - 未注入时，`qnRcProfilerFileLog()` 会回退到 [fileLog]，保持向下兼容。
     */
    var rcProfilerFileLog: ICommonLog? = null

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
fun qnLogcat(): ICommonLog? = if (isDebug() || isRdm()) QnPlatformLog.logcat else EmptyLogcatImpl()

fun qnFileLog(): ICommonLog? {
    val fileLog = QnPlatformLog.fileLog ?: return qnLogcat()
    // debug 包下同时通过 logcat 输出一份，方便实时调试
    val logcat = if (isDebug()) QnPlatformLog.logcat else null
    return if (logcat != null) DualLog(fileLog, logcat) else fileLog
}

/**
 * 同时向两个 [ICommonLog] 输出日志。
 */
private class DualLog(
    private val primary: ICommonLog,
    private val secondary: ICommonLog,
) : ICommonLog {
    override fun logV(tag: String, msg: String) {
        primary.logV(tag, msg)
        secondary.logV(tag, msg)
    }

    override fun logD(tag: String, msg: String) {
        primary.logD(tag, msg)
        secondary.logD(tag, msg)
    }

    override fun logI(tag: String, msg: String) {
        primary.logI(tag, msg)
        secondary.logI(tag, msg)
    }

    override fun logW(tag: String, msg: String) {
        primary.logW(tag, msg)
        secondary.logW(tag, msg)
    }

    override fun logE(tag: String, msg: String, throwable: Throwable?) {
        primary.logE(tag, msg, throwable)
        secondary.logE(tag, msg, throwable)
    }
}

/**
 * 视频专用文件日志：优先写入独立子目录；若未注入，回退到通用 [qnFileLog]。
 */
fun qnVideoFileLog(): ICommonLog? = QnPlatformLog.videoFileLog ?: qnFileLog()

/**
 * 重组分析专用文件日志：优先写入独立子目录；若未注入，回退到通用 [qnFileLog]。
 */
fun qnRcProfilerFileLog(): ICommonLog? = QnPlatformLog.rcProfilerFileLog ?: qnFileLog()