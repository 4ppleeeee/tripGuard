package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * Reshub SDK 初始化配置
 */
data class ReshubInitConfig(
    val appId: String,
    val appKey: String,
    val appVersion: String,
    val deviceId: String = "",
    val useTestEnv: Boolean = false,
    val forceOnlineEnv: Boolean = false,
    val localPresetResPath: String = "",
    val isDebug: Boolean = false,
) : SdkConfig
