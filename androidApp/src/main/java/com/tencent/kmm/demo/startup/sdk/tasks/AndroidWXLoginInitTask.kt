package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.WXLoginInitConfig
import com.tencent.kmm.startup.std.tasks.WXLoginInitResult

/**
 * Android 微信登录 SDK 初始化任务
 */
fun initWXLogin(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<WXLoginInitResult>
) {
    val config = context.configOrNull<WXLoginInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    val api = WXAPIFactory.createWXAPI(app, config.appId, false).apply {
        registerApp(config.appId)
    }

    AndroidWXLoginRuntime.initialize(config.appId, api)
    callback(WXLoginInitResult(config.appId))
}
