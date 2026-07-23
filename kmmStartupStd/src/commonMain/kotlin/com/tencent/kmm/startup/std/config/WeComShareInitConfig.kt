package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * 企业微信分享 SDK 初始化配置
 */
data class WeComShareInitConfig(
    val shareAppId: String,
) : SdkConfig
