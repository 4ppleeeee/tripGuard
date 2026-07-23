package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import android.os.Build
import android.util.Log
import com.tencent.beacon.event.open.BeaconConfig
import com.tencent.beacon.event.open.BeaconInitException
import com.tencent.beacon.event.open.BeaconReport
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.BeaconInitConfig
import com.tencent.kmm.startup.std.tasks.BeaconInitResult

/**
 * Android Beacon 初始化任务
 *
 * Beacon 上报初始化流程：
 * 1. 构建 BeaconConfig（关闭 pagePath / 使用隐私开关）
 * 2. 设置 channel / appVersion / 日志开关
 * 3. 调用 start 完成 SDK 初始化
 */
fun initBeacon(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<BeaconInitResult>
) {
    val config = context.configOrNull<BeaconInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    val report = BeaconReport.getInstance()
    val beaconConfig = BeaconConfig.builder()
        .pagePathEnable(false)
        .setModel(Build.MODEL)
        .auditEnable(config.userAgreePrivacy)
        .setNeedInitQimei(false)
        .build()

    val channelId = config.channelId.ifBlank { context.packageName }
    report.setChannelID(config.appKey, channelId)
    report.setAppVersion(config.appVersion)
    report.setCollectProcessInfo(true)
    report.setLogAble(config.enableLog)

    try {
        report.start(app, config.appKey, beaconConfig)
    } catch (e: BeaconInitException) {
        Log.i(TAG, "initBeacon", e)
    }

    if (config.userId.isNotBlank()) {
        report.setUserID(config.appKey, config.userId)
    }

    callback(BeaconInitResult(appKey = config.appKey))
}

private const val TAG = "BeaconInitTask"
