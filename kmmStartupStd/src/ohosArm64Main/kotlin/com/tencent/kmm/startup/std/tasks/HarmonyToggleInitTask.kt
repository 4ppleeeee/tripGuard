package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.std.config.ToggleInitConfig
import com.tencent.kmm.startup.std.hmyStartupService

private const val QIMEI_PLACEHOLDER = "qimei36"

/**
 * Harmony Shiply/Toggle 初始化任务
 */
internal fun initToggle(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<ToggleInitResult>
) {
    val config = context.configOrNull<ToggleInitConfig>() ?: return
    hmyStartupService.initToggle(
        appId = config.appId,
        appKey = config.appKey,
        appVersion = config.appVersion,
        userId = resolveUserId(config),
        deviceId = resolveDeviceId(config.deviceId),
        useTestEnv = config.useTestEnv,
        isDebug = config.isDebug,
    ) { env ->
        callback(ToggleInitResult(appId = config.appId, env = env))
    }
}

private fun resolveUserId(config: ToggleInitConfig): String {
    return config.userId.ifBlank { HarmonyToggleAccountBridge.currentUserId() }
}

private fun resolveDeviceId(deviceId: String): String {
    return when {
        deviceId.isBlank() -> QimeiState.qimei36
        deviceId.equals(QIMEI_PLACEHOLDER, ignoreCase = true) -> QimeiState.qimei36
        else -> deviceId
    }
}

object HarmonyToggleAccountBridge {
    private var userIdProvider: (() -> String)? = null

    fun setUserIdProvider(provider: () -> String) {
        userIdProvider = provider
    }

    fun currentUserId(): String {
        return userIdProvider?.invoke().orEmpty()
    }

    fun notifyUserChanged(userId: String = currentUserId()) {
        hmyStartupService.switchToggleUser(userId)
    }
}
