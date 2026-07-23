package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class BeaconInitResult(
    val appKey: String,
)

class BeaconInitTask(private val initBeacon: PlatformTask<BeaconInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(BuglyInitTask.TASK_ID)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initBeacon(context) {
            // 预留结果透传
        }
    }

    companion object {
        const val TASK_ID = "beacon"
    }
}
