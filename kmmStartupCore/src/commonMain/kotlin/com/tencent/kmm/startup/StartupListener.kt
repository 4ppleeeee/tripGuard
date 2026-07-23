package com.tencent.kmm.startup

interface StartupListener {
    fun onStartupBegin(totalTaskCount: Int) {}

    fun onTaskBegin(taskId: String, scope: StartupScope) {}

    fun onTaskCompleted(taskId: String, durationMs: Long) {}

    fun onTaskFailed(taskId: String, error: Throwable) {}

    fun onStartupCompleted(result: StartupResult) {}
}
