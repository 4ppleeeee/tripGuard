package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.trace.TuringLog
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.TuringInitConfig
import com.tencent.kmm.startup.std.hmyStartupService

/**
 * Harmony 图灵盾初始化任务
 */
internal fun initTuring(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<TuringInitResult>
) {
    val config = context.configOrNull<TuringInitConfig>()
    if (config == null) {
        TuringLog.debug { "initTuring() config为null, 跳过初始化" }
        return
    }

    if (!config.userAgreePrivacy) {
        TuringLog.debug { "initTuring() 用户未同意隐私协议, 跳过初始化" }
        callback(TuringInitResult())
        return
    }

    TuringLog.debug { "initTuring() 开始调用鸿蒙SDK, userId=${config.userId.isNotEmpty()}" }
    TuringLog.fileLog("initTuring() 调用鸿蒙SDK")
    hmyStartupService.initTuring(
        appId = config.appId,
        channelId = config.channelId,
        userId = config.userId,
        isDebug = config.isDebug,
        callback = { openIdTicket, aidTicket, taidTicket, toaid ->
            TuringLog.debug {
                "回调 openIdTicket=${openIdTicket.isNotEmpty()}" +
                " aidTicket=${aidTicket.isNotEmpty()}" +
                " taidTicket=${taidTicket.isNotEmpty()}" +
                " toaid=${toaid.isNotEmpty()}"
            }
            callback(
                TuringInitResult(
                    openIdTicket = openIdTicket,
                    aidTicket = aidTicket,
                    taidTicket = taidTicket,
                    toaid = toaid,
                )
            )
        }
    )
}
