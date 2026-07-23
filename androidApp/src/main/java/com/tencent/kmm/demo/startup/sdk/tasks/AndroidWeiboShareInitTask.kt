package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.WeiboShareInitConfig
import com.tencent.kmm.startup.std.tasks.WeiboShareInitResult

/**
 * Android 新浪微博分享 SDK 初始化任务
 */
fun initWeiboShare(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<WeiboShareInitResult>
) {
    val config = context.configOrNull<WeiboShareInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    val authInfo = AuthInfo(
        app,
        config.appKey,
        DEFAULT_REDIRECT_URL,
        ""
    )
    WBAPIFactory.createWBAPI(app).registerApp(app, authInfo)
    callback(WeiboShareInitResult(config.appKey))
}

private const val DEFAULT_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html"
