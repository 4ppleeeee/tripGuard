package com.tencent.kmm.startup.std.tasks

import com.tencent.kmm.startup.std.trace.QimeiLog
import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.std.tasks.LoggerInitTask
import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.StartupScope
import com.tencent.kmm.startup.StartupTask

data class QimeiInitResult(
    val qimei: String,
    val qimei36: String,
)

class QimeiInitTask(private val initQimei: PlatformTask<QimeiInitResult>) : StartupTask {
    override val taskId: String = TASK_ID

    override fun dependencies(): List<String> = listOf(LoggerInitTask.TASK_ID)

    override fun scope(): StartupScope = StartupScope.MAIN

    override suspend fun execute(context: StartupContext) {
        initQimei(context) { result ->
            QimeiLog.fileLog("qimei result: $result")
            QimeiState.update(result)
            QimeiStatusBridge.updateQimei36(result.qimei36)
        }
    }

    companion object {
        const val TASK_ID = "qimei"
    }
}

object QimeiStatusBridge {
    private var updater: ((String) -> Unit)? = null

    fun setUpdater(updater: (String) -> Unit) {
        this.updater = updater
    }

    fun updateQimei36(qimei36: String) {
        updater?.invoke(qimei36)
    }
}
