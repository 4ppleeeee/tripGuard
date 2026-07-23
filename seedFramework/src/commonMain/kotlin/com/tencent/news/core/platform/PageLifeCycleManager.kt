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
        lifeCycleListeners.forEach {
            it.onCreate(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onStart(context: IKmmContext) {
        log("onStart $context")
        lifeCycleListeners.forEach {
            it.onStart(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onResume(context: IKmmContext) {
        log("onResume $context")
        lifeCycleListeners.forEach {
            it.onResume(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onPause(context: IKmmContext) {
        log("onPause $context")
        lifeCycleListeners.forEach {
            it.onPause(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onStop(context: IKmmContext) {
        log("onStop $context")
        lifeCycleListeners.forEach {
            it.onStop(context)
        }
    }

    @OnlyHostInvokeApi("业务侧分发，其他地方不允许调用")
    fun onDestroy(context: IKmmContext) {
        log("onDestroy $context")
        lifeCycleListeners.forEach {
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