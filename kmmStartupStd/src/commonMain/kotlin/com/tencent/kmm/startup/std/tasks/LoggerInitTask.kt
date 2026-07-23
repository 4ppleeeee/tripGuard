package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupTask

class LoggerInitTask(private val platformTask: PlatformTask<Unit>) : StartupTask {
    override val taskId: String = TASK_ID

    override suspend fun execute(context: StartupContext) {
        platformTask(context) {}
    }

    companion object {
        const val TASK_ID = "logger"
    }
}
