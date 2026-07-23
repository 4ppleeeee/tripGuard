package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic


typealias IKmmAction = () -> Unit


interface ITask {
    fun postAction(action: IKmmAction, delayTime: Long = 0L): IKmmActionResult
    fun runIOAction(action: IKmmAction)
    fun runMainAction(action: IKmmAction)
    fun runCpuAction(action: IKmmAction)
}


interface IKmmActionResult {
    fun cancel()
}

class DefaultKmmActionResult : IKmmActionResult {
    override fun cancel() {
    }
}


fun appTask(): ITask {
    return QnPlatformLogic.task ?: syncRunTask
}

private val syncRunTask by lazy { SyncRunTask() }

class SyncRunTask : ITask {
    override fun postAction(action: IKmmAction, delayTime: Long): IKmmActionResult {
        action()
        return DefaultKmmActionResult()
    }

    override fun runMainAction(action: IKmmAction) {
        action()
    }

    override fun runIOAction(action: IKmmAction) {
        action()
    }

    override fun runCpuAction(action: IKmmAction) {
        action()
    }
}