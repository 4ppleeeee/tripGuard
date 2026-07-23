package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class UploadSdkInitResult(
    val bizAppId: Int,
    val bizDomain: String,
)

class UploadSdkInitTask(private val initUploadSdk: PlatformTask<UploadSdkInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(LoggerInitTask.TASK_ID)

    override fun scope(): StartupScope = StartupScope.ASYNC

    override suspend fun execute(context: StartupContext) {
        initUploadSdk(context) {
            // 预留结果透传
        }
    }

    companion object {
        const val TASK_ID = "uploadSdk"
    }
}
