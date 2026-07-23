package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class TabExpInitResult(
    val appId: String,
)

class TabExpInitTask(private val initTabExp: PlatformTask<TabExpInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(StandardStartupTaskIds.QIMEI)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initTabExp(context) {
            // 预留结果透传
        }
    }

    companion object {
        const val TASK_ID = StandardStartupTaskIds.TAB_EXP
    }
}
