package com.tencent.news.core

import com.tencent.news.core.ohos.utils.OhosPlatformUtil

actual fun getPlatform(): Platform = OhosPlatform

object OhosPlatform : Platform {

    override val name = getOSVersion() // 鸿蒙的 deviceinfo.osFullName，例如：OpenHarmony-5.0.5.165

    override fun getType(): PlatformType = PlatformType.HARMONY

    override fun currentThreadName(): String = ""

    override fun getOSVersion(): String = OhosPlatformUtil.getOsFullName()

    override fun getApiVersion(): Int = OhosPlatformUtil.getApiVersion()
}