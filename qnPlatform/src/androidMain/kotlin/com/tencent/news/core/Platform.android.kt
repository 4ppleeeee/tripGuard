package com.tencent.news.core

import android.os.Build
import com.tencent.news.core.platform.mock.MockIOSPlatform
import com.tencent.news.core.platform.mock.PlatformManager

class AndroidPlatform : Platform {

    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun getType() = PlatformType.ANDROID

    override fun currentThreadName(): String = Thread.currentThread().name

    override fun getOSVersion(): String = Build.VERSION.RELEASE

    override fun getApiVersion(): Int = Build.VERSION.SDK_INT
}

actual fun getPlatform(): Platform =
    if (PlatformManager.mockAsIOSPlatform) MockIOSPlatform() else AndroidPlatform()