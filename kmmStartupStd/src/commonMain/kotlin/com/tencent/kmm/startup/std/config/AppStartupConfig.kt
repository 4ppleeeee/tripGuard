package com.tencent.kmm.startup.std.config

import com.tencent.kmm.startup.Platform
import com.tencent.kmm.startup.SdkConfig
import com.tencent.kmm.startup.StartupContext

interface AppStartupConfig {
    val appId: String
    val packageName: String
    val isDebug: Boolean

    fun allConfigs(): List<SdkConfig>
}

data class BasicAppStartupConfig(
    override val appId: String,
    override val packageName: String,
    override val isDebug: Boolean,
    private val configs: List<SdkConfig> = emptyList(),
) : AppStartupConfig {
    override fun allConfigs(): List<SdkConfig> = configs
}

fun AppStartupConfig.toStartupContext(
    platform: Platform,
    appVersion: Int = 0,
    nativeContext: Any? = null,
): StartupContext {
    return StartupContext.Builder().apply {
        appId = this@toStartupContext.appId
        packageName = this@toStartupContext.packageName
        isDebug = this@toStartupContext.isDebug
        this.appVersion = appVersion
        this.platform = platform
        this.nativeContext = nativeContext
        allConfigs().forEach { addConfig(it) }
    }.build()
}
