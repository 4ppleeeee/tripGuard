package com.tencent.kmm.startup.std.trace

import com.tencent.kmm.startup.StartupListener
import com.tencent.kmm.startup.StartupResult
import com.tencent.kmm.startup.StartupScope

class LoggingStartupListener(
    private val logger: (String) -> Unit = ::println
) : StartupListener {

    override fun onStartupBegin(totalTaskCount: Int) {
        logger("[Startup] ▶ 开始，共 $totalTaskCount 个任务")
    }

    override fun onTaskBegin(taskId: String, scope: StartupScope) {
        logger("[Startup]   ├─ $taskId ($scope) 开始")
    }

    override fun onTaskCompleted(taskId: String, durationMs: Long) {
        logger("[Startup]   ├─ $taskId ✓ ${durationMs}ms")
    }

    override fun onTaskFailed(taskId: String, error: Throwable) {
        logger("[Startup]   ├─ $taskId ✗ ${error.message}")
    }

    override fun onStartupCompleted(result: StartupResult) {
        logger(
            "[Startup] ■ 完成: ${result.successCount}/${result.totalCount} 成功, 耗时 ${result.totalDurationMs}ms"
        )
        if (result.failures.isNotEmpty()) {
            result.failures.forEach { (id, err) ->
                logger("[Startup]   └─ FAILED $id: ${err.message}")
            }
        }
    }
}
