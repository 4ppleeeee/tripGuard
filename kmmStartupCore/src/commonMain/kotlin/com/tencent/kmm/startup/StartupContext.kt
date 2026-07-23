package com.tencent.kmm.startup

import kotlin.reflect.KClass

class StartupContext(
    val appId: String,
    val packageName: String,
    val isDebug: Boolean,
    val appVersion: Int = 0,
    val platform: Platform,
    val nativeContext: Any? = null,
    val configs: Map<KClass<out SdkConfig>, SdkConfig> = emptyMap()
) {

    inline fun <reified T : SdkConfig> config(): T {
        return configs[T::class] as? T
            ?: throw IllegalStateException("未注册配置: ${T::class.simpleName}")
    }

    inline fun <reified T : SdkConfig> configOrNull(): T? {
        return configs[T::class] as? T
    }

    class Builder {
        var appId: String = ""
        var packageName: String = ""
        var isDebug: Boolean = false
        var appVersion: Int = 0
        var platform: Platform = Platform.ANDROID
        var nativeContext: Any? = null
        private val configs = linkedMapOf<KClass<out SdkConfig>, SdkConfig>()

        fun addConfig(config: SdkConfig): Builder {
            configs[config::class] = config
            return this
        }

        fun build(): StartupContext {
            return StartupContext(
                appId = appId,
                packageName = packageName,
                isDebug = isDebug,
                appVersion = appVersion,
                platform = platform,
                nativeContext = nativeContext,
                configs = configs.toMap()
            )
        }
    }
}
