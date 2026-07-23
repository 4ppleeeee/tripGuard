package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.BeaconInitConfig
import com.tencent.kmm.startup.std.tasks.BeaconInitResult
import com.tencent.kmm.startup.std.config.QimeiInitConfig
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony Beacon 初始化任务
 */
internal fun initBeacon(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<BeaconInitResult>
) {
    val config = context.configOrNull<BeaconInitConfig>() ?: return
    val qimeiConfig = context.configOrNull<QimeiInitConfig>()

    hmyStartupService.initBeacon(
        appKey = config.appKey,
        appVersion = config.appVersion,
        packageName = context.packageName,
        qimeiAppKey = qimeiConfig?.appKey.orEmpty(),
        qimeiChannelId = qimeiConfig?.channelId.orEmpty(),
        isDebug = config.enableLog || context.isDebug,
        userAgreePrivacy = config.userAgreePrivacy,
    ) {
        callback(BeaconInitResult(appKey = config.appKey))
    }
}
