package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IKmmAction
import com.tencent.news.core.platform.api.IKmmActionResult
import com.tencent.news.core.platform.api.ITask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 注入鸿蒙端 ITask 实现。
 *
 * 前置依赖：在 HarmonyStartupProvider 初始化阶段已通过
 * `kotlinx.coroutines.initMainHandler(getEnv()!!)` 将 `Dispatchers.Main` 绑定到 ArkTS 主线程，
 * 因此这里可以直接基于 Kotlin 协程调度：
 *   - runMainAction : Dispatchers.Main      （ArkTS 主线程）
 *   - runIOAction   : Dispatchers.IO        （IO 线程池）
 *   - runCpuAction  : Dispatchers.Default   （CPU 计算线程池）
 *   - postAction    : 基于 Main 调度器 + delay，返回可 cancel 的 [IKmmActionResult]
 *
 * 业务层可通过 `appTask()` 获取该实例。
 */
fun setupOhosAppTask() {
    QnPlatformLogic.task = OhosAppTask
}

private object OhosAppTask : ITask {

    // 使用 SupervisorJob，单个任务异常不影响其他任务；默认绑定到 Main 调度器，
    // postAction(delay) 场景下直接复用此 scope。
    private val taskScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun postAction(action: IKmmAction, delayTime: Long): IKmmActionResult {
        val job = taskScope.launch {
            if (delayTime > 0L) {
                delay(delayTime)
            }
            action()
        }
        return OhosKmmActionResult(job)
    }

    override fun runMainAction(action: IKmmAction) {
        taskScope.launch(Dispatchers.Main) { action() }
    }

    override fun runIOAction(action: IKmmAction) {
        taskScope.launch(Dispatchers.IO) { action() }
    }

    override fun runCpuAction(action: IKmmAction) {
        taskScope.launch(Dispatchers.Default) { action() }
    }
}

private class OhosKmmActionResult(private val job: Job) : IKmmActionResult {
    override fun cancel() {
        job.cancel()
    }
}
