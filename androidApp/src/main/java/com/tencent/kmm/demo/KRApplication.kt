package com.tencent.kmm.demo

import android.app.Application
import android.content.res.Configuration
import com.tencent.kmm.startup.std.AndroidStartupBridge
import com.tencent.kmm.demo.startup.sdk.AndroidPlatformTaskProvider
import com.tencent.kmm.demo.startup.sdk.tasks.AndroidTabExpRuntime
import com.tencent.kmm.demo.startup.sdk.tasks.AndroidToggleRuntime
import com.tencent.kuikly.core.android.KuiklyCoreEntry_demoCore
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.compose.AndroidComposePageDelegate
import com.tencent.news.core.compose.debug.profiler.RecompositionProfilerManager
import com.tencent.news.core.platform.AppStateManager
import com.tencent.news.core.platform.AndroidRuntimeProvider
import com.tencent.news.core.platform.qnFileLog
import com.tencent.news.core.platform.network.ktor.initKtorNetwork
import com.tencent.news.core.platform.setupAndroidPlatformLogic
import com.tencent.kmm.demo.setup.AndroidPlatformLogicSetup
import com.tencent.kmm.demo.setup.notifySystemConfigurationChanged
import com.tencent.kmm.demo.setup.setupAndroidAppStateLifecycle
import com.tencent.kmm.demo.setup.syncAutoPlayNextOnStartup
import com.tencent.kmm.demo.setup.syncBigFontModeOnStartup
import com.tencent.kmm.demo.setup.syncDarkModeOnStartup
import com.tencent.kmm.demo.setup.BuildInfo

class KRApplication : Application(), IKmmContext {

    @OptIn(KmmInternalApi::class)
    override fun onCreate() {
        super.onCreate()
        application = this
        GlobalContext.setContext(this)
        GlobalContext.setApp(this)

        AppStateManager.onAppStart()
        setupAndroidAppStateLifecycle(this)

        BuildInfo.init(
            buildId = BuildConfig.CI_BUILD_ID,
            buildNum = BuildConfig.CI_BUILD_NUM,
            pipelineName = BuildConfig.CI_PIPELINE_NAME,
            branch = BuildConfig.CI_BRANCH,
            commit = BuildConfig.CI_COMMIT,
            buildTime = BuildConfig.CI_BUILD_TIME,
        )

        setupAndroidRuntimeProvider()
        setupAndroidPlatformLogic(this)
        AndroidPlatformLogicSetup.setup()
        AndroidComposePageDelegate.registerFactory { AndroidComposePageDelegate() }
        KuiklyCoreEntry_demoCore.triggerRegisterPages()

        initKtorNetwork()

        AndroidStartupBridge().launch(
            appId = BuildConfig.APPLICATION_ID,
            packageName = BuildConfig.APPLICATION_ID,
            isDebug = BuildConfig.DEBUG,
            appVersion = BuildConfig.VERSION_CODE,
            configs = listOf(
                qimeiConfig,
                turingConfig,
                toggleConfig,
                tabExpConfig,
                buglyConfig,
                beaconConfig,
                qqLoginConfig,
                wxLoginConfig,
                weiboShareConfig,
                weComShareConfig,
                reshubConfig,
            ),
            nativeContext = this,
            platformTaskProvider = AndroidPlatformTaskProvider(),
        )

        syncDarkModeOnStartup()
        syncBigFontModeOnStartup()
        syncAutoPlayNextOnStartup()
        RecompositionProfilerManager.restoreStateOnStartup()

        qnFileLog()?.logI(
            "BuildInfo",
            "CI buildId=${BuildConfig.CI_BUILD_ID} " +
                "buildNum=${BuildConfig.CI_BUILD_NUM} " +
                "pipeline=${BuildConfig.CI_PIPELINE_NAME} " +
                "branch=${BuildConfig.CI_BRANCH} " +
                "commit=${BuildConfig.CI_COMMIT} " +
                "buildTime=${BuildConfig.CI_BUILD_TIME}"
        )
    }

    private fun setupAndroidRuntimeProvider() {
        AndroidRuntimeProvider.shiplyStringProvider = AndroidToggleRuntime::getStringValue
        AndroidRuntimeProvider.shiplySwitchProvider = AndroidToggleRuntime::isEnable
        AndroidRuntimeProvider.tabExpIntProvider = AndroidTabExpRuntime::getTabExpInt
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        notifySystemConfigurationChanged(newConfig)
    }

    companion object {
        lateinit var application: KRApplication
    }
}
