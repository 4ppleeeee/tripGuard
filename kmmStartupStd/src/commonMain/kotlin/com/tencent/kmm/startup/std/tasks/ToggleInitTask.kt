package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class ToggleInitResult(
    val appId: String,
    val env: String,
)

class ToggleInitTask(private val initToggle: PlatformTask<ToggleInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(StandardStartupTaskIds.QIMEI)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initToggle(context) {
            // 预留结果透传
        }
    }

    companion object {
        const val TASK_ID = StandardStartupTaskIds.TOGGLE
    }
}
