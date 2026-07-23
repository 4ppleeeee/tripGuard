package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.std.config.QimeiInitConfig
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony Qimei 初始化任务
 */
internal fun initQimei(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<QimeiInitResult>
) {
    val config = context.configOrNull<QimeiInitConfig>() ?: return

    hmyStartupService.initQimei(
        config.appKey,
        config.channelId,
        config.isDebug
    ) { qimei36, qimei16 ->
        callback(
            QimeiInitResult(
                qimei = qimei16,
                qimei36 = qimei36
            )
        )
    }
}

object HarmonyQimeiUsKeyBridge {
    fun getUsKey(
        config: QimeiInitConfig,
        businessId: String,
        busInfo: String,
    ): String {
        return hmyStartupService.getUskey(
            config.appKey,
            config.appVersion,
            businessId,
            QimeiState.qimei36,
            busInfo,
        )
    }
}
