package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.AbsAppConfig
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppConfig = JSValue

fun setupOhosAppConfig(appConfig: IOhosAppConfig) {
    // 鸿蒙 Shiply 暂无可用 C API 指针，直接通过 KNCallback 读取 ArkTS ToggleManager。
    QnPlatformLogic.appConfig = OhosAppConfigProvider(appConfig.asOhosAppConfig())
}

private class OhosAppConfigProvider(
    private val appConfig: OhosAppConfig
) : AbsAppConfig() {
    override fun getShiplyConfig(key: String, defaultValue: String): String {
        return runCatching {
            appConfig.getShiplyConfig(key, defaultValue)
        }.getOrDefault(defaultValue)
    }

    override fun getShiplySwitch(key: String, defaultValue: Boolean): Boolean {
        return runCatching {
            appConfig.getShiplySwitch(key, defaultValue)
        }.getOrDefault(defaultValue)
    }

    override fun getTabExpInt(key: String, defaultValue: Int): Int {
        return defaultValue
    }
}

@KNCallback
interface OhosAppConfig {
    fun getShiplyConfig(key: String, defaultValue: String): String

    fun getShiplySwitch(key: String, defaultValue: Boolean): Boolean
}
