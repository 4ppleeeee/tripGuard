package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * 新浪微博分享 SDK 初始化配置
 */
data class WeiboShareInitConfig(
    val appKey: String,
    val universalLink: String = "",
) : SdkConfig
