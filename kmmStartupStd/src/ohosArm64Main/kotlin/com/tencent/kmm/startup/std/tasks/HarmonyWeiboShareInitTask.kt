package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.WeiboShareInitConfig
import com.tencent.kmm.startup.std.tasks.WeiboShareInitResult

/**
 * Harmony 新浪微博分享 SDK 初始化任务
 *
 * Demo 鸿蒙端未集成微博 SDK，这里保持空实现，仅做配置透传保护。
 */
internal fun initWeiboShare(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<WeiboShareInitResult>
) {
    val config = context.configOrNull<WeiboShareInitConfig>() ?: return
    callback(WeiboShareInitResult(config.appKey))
}
