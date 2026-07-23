package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.Platform
import com.tencent.kmm.startup.std.config.AppStartupConfig
import com.tencent.kmm.startup.std.config.toStartupContext
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppStatus
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppStatus
import com.tencent.news.core.ohos.setup.setupOhosAppDevice
import com.tencent.news.core.platform.configGC
import com.tencent.news.core.platform.network.ktor.initKtorNetwork

/**
 * Harmony startup facade shared by OHOS shells.
 *
 * Business-specific config and reporting stay in the host/sample layer; this
 * bridge only owns platform-neutral startup sequencing.
 */
class HarmonyStartupBridge {
    fun launch(
        isDebug: Boolean,
        nativeContext: Any?,
        startupConfig: AppStartupConfig,
        pendingAppStatus: IOhosAppStatus?,
        beforeStartupTasks: () -> Unit = {},
        afterStartupTasks: () -> Unit = {},
        platformTaskProvider: PlatformTaskProvider = HarmonyPlatformTaskProvider(),
    ) {
        configGC(interval = 10, targetHeapUtilization = 0.8)

        setupOhosAppDevice()
        pendingAppStatus?.let { setupOhosAppStatus(it, isDebug) }

        beforeStartupTasks()

        StartupKit.launchAsync(
            context = startupConfig.toStartupContext(
                platform = Platform.HARMONY,
                nativeContext = nativeContext,
            ),
            platformTaskProvider = platformTaskProvider,
        )

        afterStartupTasks()
        initKtorNetwork()
    }
}
