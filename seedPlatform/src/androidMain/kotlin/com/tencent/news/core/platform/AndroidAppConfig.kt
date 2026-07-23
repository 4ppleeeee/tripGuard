package com.tencent.news.core.platform

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.api.AbsAppConfig
import com.tencent.news.core.platform.api.IAppInitConfig

@KmmInternalApi
internal fun setupAndroidAppConfig() {
    QnPlatformLogic.appConfig = AndroidAppConfig
}

private object AndroidAppConfig : AbsAppConfig() {
    override fun getShiplyConfig(key: String, defaultValue: String): String {
        return AndroidRuntimeProvider.shiplyStringProvider(key, defaultValue)
    }

    override fun getShiplySwitch(key: String, defaultValue: Boolean): Boolean {
        return AndroidRuntimeProvider.shiplySwitchProvider(key, defaultValue)
    }

    override fun getAppInitConfig(): IAppInitConfig? {
        return null
    }

    override fun getTabExpInt(key: String, defaultValue: Int): Int {
        return AndroidRuntimeProvider.tabExpIntProvider(key, defaultValue)
    }
}
