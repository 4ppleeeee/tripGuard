package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class MidasInitResult(
    val initialized: Boolean,
    val platform: String,
)

class MidasInitTask(private val initMidas: PlatformTask<MidasInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(BeaconInitTask.TASK_ID)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initMidas(context) {
            // 预留结果透传
        }
    }

    companion object {
        const val TASK_ID = "midas"
    }
}
