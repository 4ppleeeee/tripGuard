package com.tencent.news.core

interface Platform {

    val name: String

    fun getType(): PlatformType

    fun currentThreadName(): String

    fun getOSVersion(): String

    fun getApiVersion(): Int

}

expect fun getPlatform(): Platform

enum class PlatformType() {
    ANDROID,
    IOS,
    HARMONY
}

fun getPlatformType(): PlatformType = getPlatform().getType()

fun getPlatformReportType() = when {
    isAndroidPlatform() -> "android"
    isIOSPlatform() -> "ios"
    isHarmonyPlatform() -> "harmony"
    else -> ""
}

fun getPlatformOSVersion(): String = getPlatform().getOSVersion()
fun getCurrentThreadName(): String = getPlatform().currentThreadName()

fun isAndroidPlatform(): Boolean = getPlatformType() == PlatformType.ANDROID
fun isIOSPlatform(): Boolean = getPlatformType() == PlatformType.IOS
fun isHarmonyPlatform(): Boolean = getPlatformType() == PlatformType.HARMONY

