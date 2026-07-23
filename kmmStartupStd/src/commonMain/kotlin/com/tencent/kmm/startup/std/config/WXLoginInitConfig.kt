package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * 微信登录 SDK 初始化配置
 */
data class WXLoginInitConfig(
    val appId: String,
    val universalLink: String = "",
) : SdkConfig
