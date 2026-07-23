package com.tencent.news.core.platform

import android.content.Context
import com.tencent.news.core.annotation.KmmInternalApi

/**
 * Registers Android default implementations owned by qnPlatform.
 *
 * SDK-backed values, such as Toggle and TabExp, are supplied through
 * [AndroidRuntimeProvider] by the Android shell.
 */
@OptIn(KmmInternalApi::class)
fun setupAndroidPlatformLogic(context: Context) {
    setupAndroidAppConfig()
    setupAndroidAppTask()
    setupAndroidAppDevice()
    setupAndroidAppEncoder()
    setupAndroidFileCacheManager(context)
}
