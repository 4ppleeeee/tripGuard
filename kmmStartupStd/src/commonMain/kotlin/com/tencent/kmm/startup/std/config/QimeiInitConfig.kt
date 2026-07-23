package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * Qimei SDK初始化配置
 */
data class QimeiInitConfig(
    val appKey: String,
    val appVersion: String,
    val channelId: String,
    val isDebug: Boolean = false,
    val enableLog: Boolean = isDebug,
    val userAgreePrivacy: Boolean = true
) : SdkConfig