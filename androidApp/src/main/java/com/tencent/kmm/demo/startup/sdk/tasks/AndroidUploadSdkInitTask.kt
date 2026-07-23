package com.tencent.kmm.demo.startup.sdk.tasks

import android.util.Log
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.UploadSdkInitConfig
import com.tencent.kmm.startup.std.tasks.UploadSdkInitResult

/**
 * Android 上传 SDK 初始化占位。
 *
 * Demo 壳不绑定具体业务上传域名；真实 App 可在自己的壳工程接入上传 SDK。
 */
fun initUploadSdk(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<UploadSdkInitResult>,
) {
    val config = context.configOrNull<UploadSdkInitConfig>() ?: return
    Log.i(TAG, "initUploadSdk no-op: bizAppId=${config.bizAppId}")
    callback(UploadSdkInitResult(config.bizAppId, config.bizDomain))
}

private const val TAG = "UploadSdkInitTask"
