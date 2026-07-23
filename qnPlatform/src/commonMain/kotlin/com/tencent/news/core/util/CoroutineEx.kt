package com.tencent.news.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Compatibility home for QnCore routing helpers. */
object CoroutineEx {

    fun syncRun(action: suspend CoroutineScope.() -> Unit): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            action()
        }
    }

    fun runIOAction(action: suspend CoroutineScope.() -> Unit): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            action()
        }
    }

    fun runCpuAction(action: suspend CoroutineScope.() -> Unit): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            action()
        }
    }
}
