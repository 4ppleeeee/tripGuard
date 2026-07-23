package com.tencent.kmm.demo.startup.sdk.tasks

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.ToggleInitConfig
import com.tencent.kmm.startup.std.tasks.QimeiState
import com.tencent.kmm.startup.std.tasks.ToggleInitResult

private const val TAG = "ToggleInitTask"
private const val QIMEI_PLACEHOLDER = "qimei36"

/**
 * Android Shiply/Toggle 初始化任务
 */
fun initToggle(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<ToggleInitResult>
) {
    val config = context.configOrNull<ToggleInitConfig>() ?: return
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android 启动缺少 Application nativeContext")

    runCatching {
        AndroidToggleRuntime.init(
            context = app,
            config = config,
            qimei36 = resolveQimei36(config),
            userId = resolveUserId(config),
            isMainProcess = isMainProcess(app),
        )
    }.onFailure { error ->
        Log.e(TAG, "init toggle failed", error)
    }

    val env = if (config.useTestEnv) "test" else "online"
    callback(ToggleInitResult(appId = config.appId, env = env))
}

private fun resolveUserId(config: ToggleInitConfig): String {
    return config.userId.ifBlank { AndroidToggleAccountBridge.currentUserId() }
}

private fun resolveQimei36(config: ToggleInitConfig): String {
    val configured = config.deviceId.trim()
    return when {
        configured.isEmpty() -> QimeiState.qimei36
        configured.equals(QIMEI_PLACEHOLDER, ignoreCase = true) -> QimeiState.qimei36
        else -> configured
    }
}

@Suppress("DEPRECATION")
private fun isMainProcess(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return Application.getProcessName() == context.packageName
    }

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        ?: return true
    val currentPid = Process.myPid()
    val processName = activityManager.runningAppProcesses
        ?.firstOrNull { it.pid == currentPid }
        ?.processName
        ?: return true
    return processName == context.packageName
}
