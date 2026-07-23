package com.tencent.kmm.startup.std.tasks

import android.app.Application
import com.tencent.mmkv.MMKV
import com.tencent.news.core.kmkv.setupKmkvStorage
import com.tencent.kmm.startup.std.OnReceiveStartupTaskResult
import com.tencent.kmm.startup.StartupContext

/**
 * Android MMKV initialization task.
 * Initializes MMKV with the application context, then sets up the shared KmkvStorage.
 */
fun initKmkv(
    context: StartupContext,
    callback: OnReceiveStartupTaskResult<Unit>
) {
    val app = context.nativeContext as? Application
        ?: throw IllegalStateException("Android startup requires Application as nativeContext")
    MMKV.initialize(app)
    setupKmkvStorage()
    callback(Unit)
}
