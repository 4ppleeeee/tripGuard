package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import android.os.Build
import com.tencent.feedback.anr.ANRReport
import com.tencent.feedback.eup.CrashReport
import com.tencent.qimei.sdk.QimeiSDK
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.BuglyInitConfig
import com.tencent.kmm.startup.std.tasks.BuglyInitResult
import com.tencent.kmm.startup.std.config.QimeiInitConfig

/**
 * Android Bugly 初始化任务
 *
 * Bugly CrashReporter 初始化流程：
 * 1. 初始化 CrashReport
 * 2. 设置 app channel / version / userId
 * 3. 启动 ANR 监控
 * 4. 通过 Qimei 回填 deviceId
 */
fun initBugly(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<BuglyInitResult>
) {
    val config = context.configOrNull<BuglyInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    CrashReport.setAllThreadStackEnable(app, true, true)
    CrashReport.setAppChannel(app, config.appChannel)
    CrashReport.setProductVersion(app, config.appVersion)
    if (config.buildNumber.isNotBlank()) {
        CrashReport.setRdmUuid(config.buildNumber)
    }
    if (config.userId.isNotBlank()) {
        CrashReport.setUserId(app, config.userId)
    }
    CrashReport.setDeviceModel(app, Build.MODEL)

    CrashReport.initCrashReport(app, config.appId, config.isDebug)
    ANRReport.startANRMonitor()

    val qimeiConfig = context.configOrNull<QimeiInitConfig>()
    if (qimeiConfig != null) {
        QimeiSDK.getInstance(qimeiConfig.appKey).getQimei { qimei ->
            val qimei36 = qimei.qimei36.orEmpty()
            if (qimei36.isNotBlank()) {
                CrashReport.setDeviceId(app, qimei36)
            }
        }
    }

    callback(BuglyInitResult(appId = config.appId))
}
