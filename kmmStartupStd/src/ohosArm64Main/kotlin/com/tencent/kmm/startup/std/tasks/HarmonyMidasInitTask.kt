package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.tasks.MidasInitResult

/**
 * Demo Harmony 端暂未接入 Midas。
 */
internal fun initMidas(
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
