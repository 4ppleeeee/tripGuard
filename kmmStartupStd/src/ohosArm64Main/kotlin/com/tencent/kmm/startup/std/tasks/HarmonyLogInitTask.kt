package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult

internal fun initLogger(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<Unit>
) {
    HarmonyLoggerBridge.install()
    callback(Unit)
}

object HarmonyLoggerBridge {
    private var installer: (() -> Unit)? = null

    fun setInstaller(installer: () -> Unit) {
        this.installer = installer
    }

    fun install() {
        installer?.invoke()
    }
}
