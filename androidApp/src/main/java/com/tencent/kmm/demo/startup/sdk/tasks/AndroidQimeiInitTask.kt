package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.Application
import android.util.Log
import com.tencent.qimei.sdk.QimeiSDK
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.QimeiInitConfig
import com.tencent.kmm.startup.std.tasks.QimeiInitResult

/**
 * Android Qimei 初始化任务
 */
fun initQimei(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<QimeiInitResult>
) {
    val config = context.configOrNull<QimeiInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    val sdk = QimeiSDK.getInstance(config.appKey)
    sdk.strategy
        .setUserAgreePrivacy(config.userAgreePrivacy)
        .enableOAID(config.userAgreePrivacy)
        .enableIMEI(config.userAgreePrivacy)
        .enableIMSI(config.userAgreePrivacy)
        .enableAndroidId(config.userAgreePrivacy)
        .enableMAC(config.userAgreePrivacy)
        .enableCid(config.userAgreePrivacy)
        .enableAudit(config.userAgreePrivacy)

    sdk.setAppVersion(config.appVersion)
        .setChannelID(config.channelId)
        .setLogAble(config.enableLog)
        .setLogObserver { message ->
            if (config.enableLog) {
                Log.i(TAG, message)
            }
        }
        .init(app)

    sdk.getQimei {
        val result = QimeiInitResult(
            qimei = it.qimei16.orEmpty(),
            qimei36 = it.qimei36.orEmpty()
        )
        callback(result)
    }
}

private const val TAG = "QimeiInitTask"
