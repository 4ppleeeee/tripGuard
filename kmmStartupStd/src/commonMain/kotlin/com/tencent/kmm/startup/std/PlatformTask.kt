package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.StartupContext

/**
 * SDK 初始化完之后的数据回调接口
 */
fun interface OnReceiveStartupTaskResult<Result> {
    operator fun invoke(result: Result)
}

/**
 * 平台任务接口
 * @param context 启动上下文
 * @param onResult 任务完成后的回调
 */
typealias PlatformTask<Result> = suspend (
    context: StartupContext,
    onResult: OnReceiveStartupTaskResult<Result>
) -> Unit
