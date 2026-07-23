package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class QQLoginInitResult(
    val appId: String,
)

class QQLoginInitTask(
    private val initQQLogin: PlatformTask<QQLoginInitResult>
) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(LoggerInitTask.TASK_ID)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initQQLogin(context) { _ ->
            // 预留扩展：后续接入登录态缓存或埋点时在此处理结果
        }
    }

    companion object {
        const val TASK_ID = "qq-login"
    }
}
