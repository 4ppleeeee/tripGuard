package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * 图灵盾 SDK 初始化配置
 */
data class TuringInitConfig(
    val appId: String = "",
    val channelId: Int = 105428,
    val userId: String = "",
    val userAgreePrivacy: Boolean = true,
    val isDebug: Boolean = false,
    val enableLog: Boolean = isDebug,
) : SdkConfig
