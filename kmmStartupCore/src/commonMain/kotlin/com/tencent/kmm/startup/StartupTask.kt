package com.tencent.kmm.startup

interface StartupTask {
    val taskId: String

    fun dependencies(): List<String> = emptyList()

    fun scope(): StartupScope = StartupScope.MAIN

    suspend fun execute(context: StartupContext)
}
