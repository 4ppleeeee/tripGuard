package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.BuglyInitConfig
import com.tencent.kmm.startup.std.tasks.BuglyInitResult
import com.tencent.kmm.startup.std.config.QimeiInitConfig
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony Bugly 初始化任务
 */
internal fun initBugly(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<BuglyInitResult>
) {
    val config = context.configOrNull<BuglyInitConfig>() ?: return
    val qimeiConfig = context.configOrNull<QimeiInitConfig>()

    hmyStartupService.initBugly(
        appId = config.appId,
        appKey = config.appKey,
        qimeiAppKey = qimeiConfig?.appKey.orEmpty(),
        qimeiChannelId = qimeiConfig?.channelId.orEmpty(),
        appVersion = config.appVersion,
        buildNumber = config.buildNumber,
        userId = config.userId,
        appChannel = config.appChannel,
        isDebug = config.isDebug,
    ) {
        callback(BuglyInitResult(config.appId))
    }
}
