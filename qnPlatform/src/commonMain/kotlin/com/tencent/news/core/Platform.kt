package com.tencent.news.core

import com.tencent.news.core.annotation.PlatformRawApi

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

@PlatformRawApi
fun getPlatformType(): PlatformType = getPlatform().getType()

fun getPlatformReportType() = when {
    isAndroidPlatform() -> "android"
    isIOSPlatform() -> "ios"
    isHarmonyPlatform() -> "harmony"
    else -> ""
}

fun getPlatformOSVersion(): String = getPlatform().getOSVersion()
fun getCurrentThreadName(): String = getPlatform().currentThreadName()

@PlatformRawApi
fun isAndroidPlatform(): Boolean = getPlatformType() == PlatformType.ANDROID

@PlatformRawApi
fun isIOSPlatform(): Boolean = getPlatformType() == PlatformType.IOS

@PlatformRawApi
fun isHarmonyPlatform(): Boolean = getPlatformType() == PlatformType.HARMONY

