package com.tencent.kmm.ohos.startup

import com.tencent.kmm.startup.std.HarmonyStartupBridge
import com.tencent.kmm.startup.std.setupOhosStandardStartupBridges
import com.tencent.kmm.startup.std.config.BasicAppStartupConfig
import com.tencent.news.core.kmkv.setupKmkvStorage
import com.tencent.news.core.ohos.framework.IOhosAppRouter
import com.tencent.news.core.ohos.framework.IOhosAppShare
import com.tencent.news.core.ohos.framework.setupOhosAppRouter
import com.tencent.news.core.ohos.framework.setupOhosAppShare
import com.tencent.news.core.ohos.setup.IOhosAppAlert
import com.tencent.news.core.ohos.setup.IOhosAppConfig
import com.tencent.news.core.ohos.setup.IOhosAppReport
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppGyroscope
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppLocation
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppPermission
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppResManager
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppStatus
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppStatusBar
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosAppWindowBridge
import com.tencent.news.core.ohos.setup.knoi.callbacks.IOhosSystemVolumeController
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppGyroscope
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppLocation
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppPermission
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppResManager
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppStatusBar
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppVibration
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppWindow
import com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosSystemVolumeController
import com.tencent.news.core.ohos.setup.setupOhosAppAlert
import com.tencent.news.core.ohos.setup.setupOhosAppConfig
import com.tencent.news.core.ohos.setup.setupOhosAppEncoder
import com.tencent.news.core.ohos.setup.setupOhosAppEventBus
import com.tencent.news.core.ohos.setup.setupOhosAppFileCacheManager
import com.tencent.news.core.ohos.setup.setupOhosAppFileManager
import com.tencent.news.core.ohos.setup.setupOhosAppInstallInfo
import com.tencent.news.core.ohos.setup.setupOhosAppReport
import com.tencent.news.core.ohos.setup.setupOhosAppTask
import com.tencent.news.core.ohos.setup.setupOhosAppUri
import com.tencent.news.core.platform.AppStateManager
import com.tencent.tmm.knoi.annotation.ServiceProvider
import com.tencent.tmm.knoi.getEnv
import com.tencent.tmm.knoi.type.JSValue

@ServiceProvider(singleton = true)
open class HarmonyStartupProvider {

    init {
        @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
        kotlinx.coroutines.initMainHandler(getEnv()!!)

        setupKmkvStorage()
        setupOhosAppEncoder()
        setupOhosAppInstallInfo()
        setupOhosAppVibration()
        setupOhosAppFileManager()
        setupOhosAppFileCacheManager()
        setupOhosAppUri()
        setupOhosAppTask()
        setupOhosAppEventBus()
    }

    fun setupAppConfig(appConfig: IOhosAppConfig) {
        setupOhosAppConfig(appConfig)
    }

    fun setupAppReport(appReport: IOhosAppReport) {
        setupOhosAppReport(appReport)
    }

    fun setupAppShare(appShare: IOhosAppShare) {
        setupOhosAppShare(appShare)
    }

    fun setupAppResManager(resManager: IOhosAppResManager) {
        setupOhosAppResManager(resManager)
    }

    fun setupAppRouter(router: IOhosAppRouter) {
        setupOhosAppRouter(router)
    }

    fun setupAppAlert(alert: IOhosAppAlert) {
        setupOhosAppAlert(alert)
    }

    fun setupAppGyroscope(gyroscope: IOhosAppGyroscope) {
        setupOhosAppGyroscope(gyroscope)
    }

    fun setupAppStatusBar(statusBar: IOhosAppStatusBar) {
        setupOhosAppStatusBar(statusBar)
    }

    fun setupAppWindow(bridge: IOhosAppWindowBridge) {
        setupOhosAppWindow(bridge)
    }

    fun setupSystemVolumeController(volumeController: IOhosSystemVolumeController) {
        setupOhosSystemVolumeController(volumeController)
    }

    fun setupAppPermission(permission: IOhosAppPermission) {
        setupOhosAppPermission(permission)
    }

    fun setupAppLocation(location: IOhosAppLocation) {
        setupOhosAppLocation(location)
    }

    private var pendingAppStatus: IOhosAppStatus? = null

    fun setupAppStatus(status: IOhosAppStatus) {
        pendingAppStatus = status
    }

    fun onAppStartup(
        isDebug: Boolean,
        appId: String,
        packageName: String,
        appVersion: String,
        nativeContext: JSValue? = null,
    ) {
        AppStateManager.onAppStart()
        val appStatus = pendingAppStatus
        pendingAppStatus = null
        HarmonyStartupBridge().launch(
            isDebug = isDebug,
            nativeContext = nativeContext,
            startupConfig = BasicAppStartupConfig(
                appId = appId,
                packageName = packageName,
                isDebug = isDebug,
                configs = emptyList(),
            ),
            pendingAppStatus = appStatus,
            beforeStartupTasks = {
                setupOhosStandardStartupBridges()
            },
        )
    }

    fun onAppForeground() {
        AppStateManager.onForeground()
    }

    fun onAppBackground() {
        AppStateManager.onBackground()
    }
}
