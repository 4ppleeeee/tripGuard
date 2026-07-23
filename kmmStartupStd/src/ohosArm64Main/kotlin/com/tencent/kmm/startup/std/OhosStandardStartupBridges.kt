package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.std.tasks.HarmonyLoggerBridge
import com.tencent.kmm.startup.std.tasks.QimeiStatusBridge
import com.tencent.kmm.startup.std.tasks.TuringOaidBridge
import com.tencent.news.core.platform.ICommonLog
import com.tencent.news.core.platform.QnPlatformLog
import com.tencent.news.core.service.AppService
import platform.ohos.LOG_APP
import platform.ohos.LOG_DEBUG
import platform.ohos.LOG_ERROR
import platform.ohos.LOG_INFO
import platform.ohos.LOG_WARN
import platform.ohos.LogLevel
import platform.ohos.OH_LOG_Print

fun setupOhosStandardStartupBridges() {
    QimeiStatusBridge.setUpdater { AppService.status.setQIMEI36(it) }
    TuringOaidBridge.setFetcher { "" }
    HarmonyLoggerBridge.setInstaller {
        QnPlatformLog.logcat = OhosLogcatImpl()
        QnPlatformLog.fileLog = OhosFileLogImpl()
    }
}

private fun platformLog(tag: String, msg: String) {
    OH_LOG_Print(LOG_APP, LOG_INFO, 1u, "KMM:$tag", msg)
}

private class OhosLogcatImpl : ICommonLog {
    override fun logV(tag: String, msg: String) = logcat(LOG_DEBUG, tag, msg)
    override fun logD(tag: String, msg: String) = logcat(LOG_DEBUG, tag, msg)
    override fun logI(tag: String, msg: String) = logcat(LOG_INFO, tag, msg)
    override fun logW(tag: String, msg: String) = logcat(LOG_WARN, tag, msg)
    override fun logE(tag: String, msg: String, throwable: Throwable?) = logcat(LOG_ERROR, tag, "$msg, $throwable")

    private fun logcat(level: LogLevel, tag: String, msg: String) {
        OH_LOG_Print(LOG_APP, level, 1u, "KMM:$tag", msg)
    }
}

private class OhosFileLogImpl : ICommonLog {
    override fun logV(tag: String, msg: String) = platformLog(tag, msg)
    override fun logD(tag: String, msg: String) = platformLog(tag, msg)
    override fun logI(tag: String, msg: String) = platformLog(tag, msg)
    override fun logW(tag: String, msg: String) = platformLog(tag, msg)
    override fun logE(tag: String, msg: String, throwable: Throwable?) = platformLog(tag, "$msg, $throwable")
}
