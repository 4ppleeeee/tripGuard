package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * TAB/Roma AB 实验 SDK 初始化配置
 */
data class TabExpInitConfig(
    val appId: String,
    val appKey: String = "",
    val sceneId: String = "",
    val appVersion: String = "",
) : SdkConfig
