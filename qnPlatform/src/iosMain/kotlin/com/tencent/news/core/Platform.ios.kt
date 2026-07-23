package com.tencent.news.core

import platform.UIKit.UIDevice

class IOSPlatform : Platform {

    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override fun getType() = PlatformType.IOS

    override fun currentThreadName(): String = ""

    override fun getOSVersion(): String = UIDevice.currentDevice.systemVersion

    override fun getApiVersion(): Int {
        val version = UIDevice.currentDevice.systemVersion
        return version.split(".").firstOrNull()?.toIntOrNull() ?: 0
    }
}

actual fun getPlatform(): Platform = IOSPlatform()