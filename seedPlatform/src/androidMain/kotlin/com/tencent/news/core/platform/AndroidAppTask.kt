package com.tencent.news.core.platform

import android.os.Handler
import android.os.Looper
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.api.DefaultKmmActionResult
import com.tencent.news.core.platform.api.IKmmAction
import com.tencent.news.core.platform.api.IKmmActionResult
import com.tencent.news.core.platform.api.ITask

@KmmInternalApi
internal fun setupAndroidAppTask() {
    QnPlatformLogic.task = AndroidAppTask
}

private object AndroidAppTask : ITask {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun postAction(action: IKmmAction, delayTime: Long): IKmmActionResult {
        mainHandler.postDelayed(action, delayTime)
        return DefaultKmmActionResult()
    }

    override fun runIOAction(action: IKmmAction) {
        Thread {
            action()
        }.start()
    }

    override fun runMainAction(action: IKmmAction) {
        postAction(action)
    }

    override fun runCpuAction(action: IKmmAction) {
        action()
    }

}
