package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.SdkConfig

/**
 * VME 上传中台 SDK 初始化配置。
 */
data class UploadSdkInitConfig(
    val bizAppId: Int = 0,
    val bizDomain: String = "",
    val ipv6Domain: String = "",
    val debugLogToFile: Boolean = false,
    val useTestEnv: Boolean = false,
) : SdkConfig
