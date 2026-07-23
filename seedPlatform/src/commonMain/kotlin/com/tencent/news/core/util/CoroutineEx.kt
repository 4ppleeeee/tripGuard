package com.tencent.news.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO // 注意这个不能删，ios和鸿蒙打包会有问题
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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