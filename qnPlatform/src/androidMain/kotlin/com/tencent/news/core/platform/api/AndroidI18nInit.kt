package com.tencent.news.core.platform.api

import android.content.Context

/**
 * Android 端 i18n 初始化。
 * 在 Android 应用启动时（如 Application.onCreate()）调用此函数。
 */
fun initAndroidI18n(context: Context) {
    AppI18nHolder.set(AndroidAppI18n(context))
}
