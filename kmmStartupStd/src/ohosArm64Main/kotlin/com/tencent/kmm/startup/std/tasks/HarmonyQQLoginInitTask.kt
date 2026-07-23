package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.QQLoginInitConfig
import com.tencent.kmm.startup.std.tasks.QQLoginInitResult
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony QQ 登录 SDK 初始化任务
 */
internal fun initQQLogin(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<QQLoginInitResult>
) {
    val config = context.configOrNull<QQLoginInitConfig>() ?: return
    hmyStartupService.initQQLogin(config.appId) {
        callback(QQLoginInitResult(config.appId))
    }
}
