package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * Beacon 初始化配置
 */
data class BeaconInitConfig(
    val appKey: String,
    val appVersion: String,
    val channelId: String = "",
    val userId: String = "",
    val userAgreePrivacy: Boolean = true,
    val enableLog: Boolean = false,
) : SdkConfig
