package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import android.os.Build
import android.os.Process
import android.util.Log
import com.tencent.rdelivery.dependency.AbsLog
import com.tencent.rdelivery.reshub.api.ResHubParams
import com.tencent.rdelivery.reshub.api.TargetType
import com.tencent.rdelivery.reshub.core.ResHubCenter
import com.tencent.rdelivery.reshub.net.ResHubDefaultDownloadImpl
import com.tencent.rdelivery.reshub.report.ResHubDefaultReportImpl
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.ReshubInitConfig
import com.tencent.kmm.startup.std.tasks.ReshubInitResult

/**
 * Android Reshub 初始化任务
 *
 * Reshub 初始化核心逻辑：
 * 1. 初始化 ResHubCenter（下载/上报实现）
 * 2. 配置独立内置资源配置开关
 * 3. 根据构建环境决定资源环境（online / test）
 * 4. 预创建业务 ResHub 实例
 */
fun initReshub(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<ReshubInitResult>
) {
    val config = context.configOrNull<ReshubInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    val params = ResHubParams(
        appVersion = config.appVersion,
        deviceId = config.deviceId,
        isRdmTest = config.isDebug,
        is64Bit = is64BitProcess()
    )

    ResHubCenter.init(
        context = app,
        params = params,
        downloadDelegate = ResHubDefaultDownloadImpl(),
        reportDelegate = ResHubDefaultReportImpl()
    )

    ResHubCenter.enableSeparateBuiltInConfigFile = true
    if (config.isDebug) {
        ResHubCenter.logDelegate = StartupResHubLogDelegate()
    }

    val env = resolveEnv(config)
    ResHubCenter.getResHub(
        config.appId,
        config.appKey,
        TargetType.AndroidApp,
        env
    )
    callback(ReshubInitResult(appId = config.appId, env = env))
}

private fun resolveEnv(config: ReshubInitConfig): String {
    if (config.forceOnlineEnv) {
        return ResHubCenter.ENV_ONLINE
    }
    return if (config.useTestEnv) {
        ResHubCenter.ENV_TEST
    } else {
        ResHubCenter.ENV_ONLINE
    }
}

private fun is64BitProcess(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Process.is64Bit()
    } else {
        false
    }
}

private class StartupResHubLogDelegate : AbsLog() {
    override fun log(tag: String?, logLevel: Level, msg: String?) {
        when (logLevel) {
            Level.VERBOSE -> Log.v(tag, msg.orEmpty())
            Level.DEBUG -> Log.d(tag, msg.orEmpty())
            Level.INFO -> Log.i(tag, msg.orEmpty())
            Level.WARN -> Log.w(tag, msg.orEmpty())
            Level.ERROR -> Log.e(tag, msg.orEmpty())
        }
    }

    override fun log(tag: String?, logLevel: Level, msg: String?, throwable: Throwable?) {
        when (logLevel) {
            Level.VERBOSE -> Log.v(tag, msg.orEmpty(), throwable)
            Level.DEBUG -> Log.d(tag, msg.orEmpty(), throwable)
            Level.INFO -> Log.i(tag, msg.orEmpty(), throwable)
            Level.WARN -> Log.w(tag, msg.orEmpty(), throwable)
            Level.ERROR -> Log.e(tag, msg.orEmpty(), throwable)
        }
    }
}
