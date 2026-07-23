package com.tencent.kmm.startup.std

import android.content.Context
import com.tencent.kmm.startup.Platform
import com.tencent.kmm.startup.SdkConfig
import com.tencent.kmm.startup.std.config.BasicAppStartupConfig
import com.tencent.kmm.startup.std.config.toStartupContext

class AndroidStartupBridge {
    fun launch(
        appId: String,
        packageName: String,
        isDebug: Boolean,
        appVersion: Int,
        configs: List<SdkConfig>,
        nativeContext: Context,
        platformTaskProvider: PlatformTaskProvider,
    ) {
        val startupContext = BasicAppStartupConfig(
            appId = appId,
            packageName = packageName,
            isDebug = isDebug,
            configs = configs,
        ).toStartupContext(
            platform = Platform.ANDROID,
            appVersion = appVersion,
            nativeContext = nativeContext,
        )

        StartupKit.launch(
            context = startupContext,
            platformTaskProvider = platformTaskProvider,
        )
    }
}
