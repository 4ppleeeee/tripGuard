package com.tencent.kmm.demo.startup.sdk.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.tasks.MidasInitResult

/**
 * Android 端 Midas 仅做 SDK 集成，不做全局启动初始化。
 */
fun initMidas(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<MidasInitResult>
) {
    callback(
        MidasInitResult(
            initialized = false,
            platform = context.platform.name.lowercase()
        )
    )
}
