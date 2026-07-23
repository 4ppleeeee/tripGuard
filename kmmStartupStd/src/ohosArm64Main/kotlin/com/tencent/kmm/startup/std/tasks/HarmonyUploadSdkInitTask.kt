package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.UploadSdkInitConfig
import com.tencent.kmm.startup.std.tasks.UploadSdkInitResult
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * 鸿蒙端无 VME 原生上传 SDK，初始化仅同步上传中台常量给 ArkTS 层。
 */
internal fun initUploadSdk(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<UploadSdkInitResult>,
) {
    val config = context.configOrNull<UploadSdkInitConfig>() ?: return
    hmyStartupService.initUploadSdk(
        bizAppId = config.bizAppId,
        bizDomain = config.bizDomain,
        callback = {
            callback(UploadSdkInitResult(config.bizAppId, config.bizDomain))
        },
    )
}
