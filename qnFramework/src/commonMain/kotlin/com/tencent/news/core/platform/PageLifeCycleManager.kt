package com.tencent.news.core.platform

import com.tencent.news.core.annotation.OnlyHostInvokeApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.extension.ConcurrentList
import com.tencent.news.core.list.trace.LifecycleLog

object PageLifeCycleManager {
    private val lifeCycleListeners = ConcurrentList<IPageLifeCycleListener>()

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onCreate(context: IKmmContext) {
        log("onCreate $context")
        safeForEach {
            it.onCreate(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onStart(context: IKmmContext) {
        log("onStart $context")
        safeForEach {
            it.onStart(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onResume(context: IKmmContext) {
        log("onResume $context")
        safeForEach {
            it.onResume(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onPause(context: IKmmContext) {
        log("onPause $context")
        safeForEach {
            it.onPause(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onStop(context: IKmmContext) {
        log("onStop $context")
        safeForEach {
            it.onStop(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onDestroy(context: IKmmContext) {
        log("onDestroy $context")
        safeForEach {
            it.onDestroy(context)
        }
    }

    fun registerLifeCycleListener(listener: IPageLifeCycleListener) {
        log("registerLifeCycleListener $listener")
        lifeCycleListeners.add(listener)
    }

    fun removeLifeCycleListener(listener: IPageLifeCycleListener) {
        log("removeLifeCycleListener $listener")
        lifeCycleListeners.remove(listener)
    }

    private fun safeForEach(action: (listener: IPageLifeCycleListener) -> Unit) {
        // 监听器可能在回调中注销自身；回调必须在 ConcurrentList 的锁外执行。
        lifeCycleListeners.shallowCopyList().forEach { action(it) }
    }

    private fun log(msg: String) {
        LifecycleLog.debug("PageLifeCycleManager") { msg }
    }
}

interface IPageLifeCycleListener {
    fun onCreate(context: IKmmContext) {}
    fun onStart(context: IKmmContext) {}
    fun onResume(context: IKmmContext) {}
    fun onPause(context: IKmmContext) {}
    fun onStop(context: IKmmContext) {}
    fun onDestroy(context: IKmmContext) {}
}
