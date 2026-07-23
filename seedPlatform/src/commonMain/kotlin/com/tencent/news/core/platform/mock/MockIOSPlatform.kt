package com.tencent.news.core.platform.mock

import com.tencent.news.core.Platform
import com.tencent.news.core.PlatformType


object PlatformManager {
    // 全局静态标记，用于单测,只在开发模式下允许mock
    var mockAsIOSPlatform: Boolean = false
}

class MockIOSPlatform : Platform {

    override val name: String = "iOS18.0"

    override fun getType() = PlatformType.IOS

    override fun currentThreadName(): String = "ios thread"

    override fun getOSVersion(): String = "18.0"

    override fun getApiVersion(): Int = 16
}

