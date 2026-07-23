package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.ReshubInitConfig
import com.tencent.kmm.startup.std.tasks.ReshubInitResult
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony Reshub 初始化任务
 */
internal fun initReshub(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<ReshubInitResult>
) {
    val config = context.configOrNull<ReshubInitConfig>() ?: return
    hmyStartupService.initReshub(
        appId = config.appId,
        appKey = config.appKey,
        appVersion = config.appVersion,
        qimei = config.deviceId,
        useTestEnv = config.useTestEnv,
        isDebug = config.isDebug,
    ) { env ->
        callback(ReshubInitResult(config.appId, env))
    }
}
