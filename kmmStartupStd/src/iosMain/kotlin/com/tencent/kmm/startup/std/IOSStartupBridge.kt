package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.Platform
import com.tencent.kmm.startup.SdkConfig
import com.tencent.kmm.startup.std.config.BasicAppStartupConfig
import com.tencent.kmm.startup.std.config.toStartupContext
import com.tencent.news.core.platform.configGC

class IOSStartupBridge {
    fun launch(
        appId: String,
        packageName: String,
        isDebug: Boolean,
        configs: List<SdkConfig>,
        platformTaskProvider: PlatformTaskProvider,
    ) {
        // Configure Kotlin/Native GC early to reduce list scrolling stalls and heap growth.
        configGC(interval = 10, targetHeapUtilization = 0.65)

        val startupContext = BasicAppStartupConfig(
            appId = appId,
            packageName = packageName,
            isDebug = isDebug,
            configs = configs,
        ).toStartupContext(platform = Platform.IOS)

        StartupKit.launch(
            context = startupContext,
            platformTaskProvider = platformTaskProvider,
        )
    }
}
