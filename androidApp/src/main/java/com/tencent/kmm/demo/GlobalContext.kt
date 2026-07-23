package com.tencent.kmm.demo

import android.app.Application
import android.content.Context

/**
 * 全局 Context 持有类，在 Application.onCreate() 中注入 Application 实例。
 * Android 全局 Context 持有器。
 */
object GlobalContext {

    private var sContext: Context? = null
    private var sApp: Application? = null

    fun setContext(context: Context) {
        sContext = context
    }

    fun getContext(): Context {
        return checkNotNull(sContext) { "GlobalContext 未初始化，请确保在 Application.onCreate() 中调用 GlobalContext.setContext()" }
    }

    fun setApp(application: Application) {
        sApp = application
    }

    fun getApp(): Application {
        return checkNotNull(sApp) { "GlobalContext 未初始化，请确保在 Application.onCreate() 中调用 GlobalContext.setApp()" }
    }
}
