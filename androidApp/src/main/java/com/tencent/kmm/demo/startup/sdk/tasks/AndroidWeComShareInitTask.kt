package com.tencent.kmm.demo.startup.sdk.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.WeComShareInitConfig
import com.tencent.kmm.startup.std.tasks.WeComShareInitResult

/**
 * Android 企业微信分享初始化占位。
 *
 * Demo 壳不再内置私有 wwapi 坐标；真实 App 可在自己的壳工程接入企业微信 SDK。
 */
fun initWeComShare(
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
