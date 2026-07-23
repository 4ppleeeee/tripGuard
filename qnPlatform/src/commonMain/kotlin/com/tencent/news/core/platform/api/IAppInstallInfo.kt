package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

interface IAppInstallInfo {
    fun isYuanbaoInstalled(): Boolean = false

    // 当前平台是否支持跳转元宝APP；鸿蒙端无该能力，需覆盖返回 false
    fun canJumpYuanbao(): Boolean = true
}

fun appInstallInfo(): IAppInstallInfo {
    val appInstallInfo = QnPlatformLogic.appInstallInfo ?: defaultAppInstallInfo
    return appInstallInfo
}

private val defaultAppInstallInfo by lazy { DefaultAppInstallInfo() }

class DefaultAppInstallInfo : IAppInstallInfo {
    override fun isYuanbaoInstalled(): Boolean {
        return false
    }
}