package com.tencent.kmm.demo.startup.sdk.logger

import android.content.Context
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.news.core.platform.AndroidLogcat
import com.tencent.news.core.platform.ICommonLog
import com.tencent.news.core.platform.QnPlatformLog
import com.tencent.kmm.demo.library.log.WsLogger
import com.tencent.kmm.demo.library.log.WsRcProfilerLogger
import com.tencent.kmm.demo.library.log.WsVideoLogger

fun initLogger(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<Unit>
) {
    QnPlatformLog.logcat = AndroidLogcat
    WsLogger.setup(context.nativeContext as? Context, context.isDebug)
    QnPlatformLog.fileLog = WsFileLogger()

    WsVideoLogger.setup(context.nativeContext as? Context, context.isDebug)
    QnPlatformLog.videoFileLog = WsVideoFileLogger()

    WsRcProfilerLogger.setup(context.nativeContext as? Context, context.isDebug)
    QnPlatformLog.rcProfilerFileLog = WsRcProfilerFileLogger()
}

private class WsFileLogger : ICommonLog {
    override fun logV(tag: String, msg: String) = WsLogger.d(tag, msg)
    override fun logD(tag: String, msg: String) = WsLogger.d(tag, msg)
    override fun logI(tag: String, msg: String) = WsLogger.i(tag, msg)
    override fun logW(tag: String, msg: String) = WsLogger.w(tag, msg)
    override fun logE(tag: String, msg: String, throwable: Throwable?) =
        WsLogger.e(tag, msg, throwable)
}

/** 将 ICommonLog 桥接到 WsVideoLogger（独立 `video/` 子目录） */
private class WsVideoFileLogger : ICommonLog {
    override fun logV(tag: String, msg: String) = WsVideoLogger.d(tag, msg)
    override fun logD(tag: String, msg: String) = WsVideoLogger.d(tag, msg)
    override fun logI(tag: String, msg: String) = WsVideoLogger.i(tag, msg)
    override fun logW(tag: String, msg: String) = WsVideoLogger.w(tag, msg)
    override fun logE(tag: String, msg: String, throwable: Throwable?) =
        WsVideoLogger.e(tag, msg, throwable)
}

/** 将 ICommonLog 桥接到 WsRcProfilerLogger（独立 `rcprofiler/` 子目录） */
private class WsRcProfilerFileLogger : ICommonLog {
    override fun logV(tag: String, msg: String) = WsRcProfilerLogger.d(tag, msg)
    override fun logD(tag: String, msg: String) = WsRcProfilerLogger.d(tag, msg)
    override fun logI(tag: String, msg: String) = WsRcProfilerLogger.i(tag, msg)
    override fun logW(tag: String, msg: String) = WsRcProfilerLogger.w(tag, msg)
    override fun logE(tag: String, msg: String, throwable: Throwable?) =
        WsRcProfilerLogger.e(tag, msg, throwable)
}
