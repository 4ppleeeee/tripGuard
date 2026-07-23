package com.tencent.kmm.demo.startup.sdk.tasks

import com.tencent.kmm.startup.std.trace.TuringLog
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.TuringInitConfig
import com.tencent.kmm.startup.std.tasks.TuringInitResult

/**
 * Android 图灵盾初始化占位。
 *
 * Demo 壳不再内置私有 turingfd 坐标；真实 App 可在自己的壳工程接入图灵盾。
 */
fun initTuring(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<TuringInitResult>
) {
    val config = context.configOrNull<TuringInitConfig>()
    if (config == null) {
        TuringLog.debug { "initTuring() config为null, 跳过初始化" }
        return
    }
    TuringLog.debug { "initTuring() demo no-op, appId=${config.appId}" }
    callback(TuringInitResult())
}
