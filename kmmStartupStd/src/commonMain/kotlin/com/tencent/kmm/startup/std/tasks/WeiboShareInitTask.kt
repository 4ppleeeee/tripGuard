package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class WeiboShareInitResult(
    val appKey: String,
)

class WeiboShareInitTask(
    private val initWeiboShare: PlatformTask<WeiboShareInitResult>
) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(LoggerInitTask.TASK_ID)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initWeiboShare(context) {
            // 预留扩展：后续接入微博分享链路埋点时在此处理结果
        }
    }

    companion object {
        const val TASK_ID = "weibo-share"
    }
}
