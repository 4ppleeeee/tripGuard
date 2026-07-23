package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * QQ 登录 SDK 初始化配置
 */
data class QQLoginInitConfig(
    val appId: String,
) : SdkConfig
