package com.tencent.kmm.demo.setup

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.tencent.news.core.platform.AppStateManager

/**
 * Android 宿主应用前后台桥接。
 *
 * 复用新闻端“可见 Activity 计数”的判断方式：首个 Activity started 视为进前台，
 * 所有 Activity stopped 后视为退后台，再统一转发给 KMM AppStateManager。
 */
fun setupAndroidAppStateLifecycle(app: Application) {
    app.registerActivityLifecycleCallbacks(AndroidAppStateLifecycleCallbacks())
}

private class AndroidAppStateLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    private var startedActivityCount = 0
    private var isAppForeground = false
    private var launchIntentResolved = false

    override fun onActivityStarted(activity: Activity) {
        startedActivityCount++
        if (!isAppForeground && startedActivityCount > 0) {
            isAppForeground = true
            syncSystemConfigurationOnForeground()
            AppStateManager.onForeground()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount--
        if (startedActivityCount <= 0 && isAppForeground) {
            startedActivityCount = 0
            isAppForeground = false
            AppStateManager.onBackground()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (!launchIntentResolved) {
            launchIntentResolved = true
            notifyAndroidLaunchIntent(activity.intent)
        }
    }
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
