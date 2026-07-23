package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

/**
 * Bugly 初始化任务执行结果
 */
data class BuglyInitResult(
    val appId: String,
)

class BuglyInitTask(private val initBugly: PlatformTask<BuglyInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(StandardStartupTaskIds.QIMEI)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initBugly(context) {
            // 预留结果透传，当前无需处理
        }
    }

    companion object {
        const val TASK_ID = StandardStartupTaskIds.BUGLY
    }
}
