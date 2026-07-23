package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * Bugly SDK 初始化配置
 */
data class BuglyInitConfig(
    val appId: String,
    val appKey: String = "",
    val appVersion: String,
    val buildNumber: String = "",
    val appChannel: String = "",
    val userId: String = "",
    val isDebug: Boolean = false,
) : SdkConfig