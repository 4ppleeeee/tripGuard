package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.TabExpInitConfig
import com.tencent.kmm.startup.std.tasks.TabExpInitResult
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony TAB/Roma AB 实验 SDK 初始化任务
 */
internal fun initTabExp(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<TabExpInitResult>
) {
    val config = context.configOrNull<TabExpInitConfig>() ?: return
    hmyStartupService.initTabExp(
        appId = config.appId,
        appVersion = config.appVersion,
        isDebug = context.isDebug
    ) {
        callback(TabExpInitResult(config.appId))
    }
}
