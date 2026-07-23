package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * Shiply/Toggle SDK 初始化配置
 */
data class ToggleInitConfig(
    val appId: String,
    val appKey: String,
    val appVersion: String,
    val userId: String = "",
    val deviceId: String = "",
    val useTestEnv: Boolean = false,
    val isDebug: Boolean = false,
) : SdkConfig
