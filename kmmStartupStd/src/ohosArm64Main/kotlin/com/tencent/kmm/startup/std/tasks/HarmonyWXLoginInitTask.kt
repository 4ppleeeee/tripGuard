package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.WXLoginInitConfig
import com.tencent.kmm.startup.std.tasks.WXLoginInitResult
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony 微信登录 SDK 初始化任务
 */
internal fun initWXLogin(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<WXLoginInitResult>
) {
    val config = context.configOrNull<WXLoginInitConfig>() ?: return
    hmyStartupService.initWXLogin(config.appId) {
        callback(WXLoginInitResult(config.appId))
    }
}
