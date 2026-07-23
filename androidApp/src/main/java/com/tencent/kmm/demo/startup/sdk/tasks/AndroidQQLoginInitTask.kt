package com.tencent.kmm.demo.startup.sdk.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.QQLoginInitConfig
import com.tencent.kmm.startup.std.tasks.QQLoginInitResult

/**
 * Android QQ 登录初始化占位。
 *
 * Demo 壳不再内置私有 open_sdk 坐标；真实 App 可在自己的壳工程接入 QQ SDK。
 */
fun initQQLogin(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<QQLoginInitResult>
) {
    val config = context.configOrNull<QQLoginInitConfig>() ?: return
    callback(QQLoginInitResult(config.appId))
}
