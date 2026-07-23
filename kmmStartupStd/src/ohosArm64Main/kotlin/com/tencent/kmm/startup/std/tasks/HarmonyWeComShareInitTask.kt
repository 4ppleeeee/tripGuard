package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.WeComShareInitConfig
import com.tencent.kmm.startup.std.tasks.WeComShareInitResult

/**
 * Harmony 企业微信分享 SDK 初始化任务
 *
 * Demo 鸿蒙端未集成企业微信 SDK，这里保持空实现，仅做配置透传保护。
 */
internal fun initWeComShare(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<WeComShareInitResult>
) {
    val config = context.configOrNull<WeComShareInitConfig>() ?: return
    callback(
        WeComShareInitResult(
            shareAppId = config.shareAppId,
        )
    )
}
