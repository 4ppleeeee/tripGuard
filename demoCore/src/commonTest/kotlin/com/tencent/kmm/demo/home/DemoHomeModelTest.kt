package com.tencent.kmm.demo.home

import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.push.NotificationAuthorizationStatus
import com.tencent.news.core.push.guide.INotificationGuideConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DemoHomeModelTest {

    @Test
    fun providesSeedMainTabEntry() {
        val entries = buildDemoEntries()

        assertEquals("/page/main_tab", entries.first().pageName)
        assertEquals("qnFramework 品字形页面", entries.first().title)
    }

    @Test
    fun coversQnPlatformInjectionPoints() {
        val capabilities = buildPlatformCapabilityItems()

        assertEquals(25, capabilities.size)
        assertTrue(capabilities.any { it.key == "network" && it.title == "网络请求" })
        assertTrue(capabilities.any { it.key == "screenInfo" && it.title == "屏幕信息" })
    }

    @Test
    fun platformTestRegistryCoversAllInjectionPointsAndMethods() {
        val testGroups = buildPlatformCapabilityTestGroups()

        assertEquals(buildPlatformCapabilityItems().map { it.key }.toSet(), testGroups.map { it.key }.toSet())
        assertTrue(testGroups.first { it.key == "network" }.methodNames.contains("postPb"))
        assertTrue(testGroups.first { it.key == "appLocation" }.methodNames.contains("requestLocation"))
        assertTrue(testGroups.first { it.key == "appWindow" }.methodNames.contains("setScreenOrientation"))
        assertTrue(testGroups.first { it.key == "appReport" }.methodNames.contains("prepareFeedbackLogZipBase64"))
        assertTrue(testGroups.filter { it.detail is PlatformCapabilityTestListDetail }.all { it.methodNames.isNotEmpty() })
    }

    @Test
    fun platformTestGroupsExposeConcreteCasesWithoutGlobalAllEntry() {
        val testGroups = buildPlatformCapabilityTestGroups()

        assertTrue(testGroups.none { it.key == "all" || it.title == "全部" })
        assertTrue(testGroups.all { group ->
            val detail = group.detail
            detail !is PlatformCapabilityTestListDetail || detail.testCases.map { it.name } == group.methodNames
        })
    }

    @Test
    fun appDeviceGroupDisplaysDeviceInfoDirectlyWithoutRunActions() {
        val appDeviceGroup = buildPlatformCapabilityTestGroups().first { it.key == "appDevice" }
        val detail = appDeviceGroup.detail

        assertTrue(detail is PlatformCapabilityDeviceInfoDetail)
        assertFalse(detail.showCloseAction)
        assertTrue(detail.displayItems.isNotEmpty())
        assertTrue(detail.displayItems.none { it.title == "getHarmonyRom" })
    }

    @Test
    fun appStatusGroupDisplaysReadonlyStatusDirectlyWithoutRunActions() {
        val appStatusGroup = buildPlatformCapabilityTestGroups().first { it.key == "appStatus" }
        val detail = appStatusGroup.detail

        assertTrue(detail is PlatformCapabilityAppStatusDetail)
        assertFalse(detail.showCloseAction)
        assertTrue(detail.displayItems.none { it.title.startsWith("set") })
        assertTrue(detail.displayItems.none { it.title.startsWith("subscribe") })
        assertTrue(detail.displayItems.none { it.title.startsWith("add") || it.title.startsWith("remove") })
    }

    @Test
    fun appStatusGroupMarksBusinessSourcedValuesAsUnsupported() {
        withFakeAppStatus {
            val appStatusGroup = buildPlatformCapabilityTestGroups().first { it.key == "appStatus" }
            val detail = appStatusGroup.detail as PlatformCapabilityAppStatusDetail

            assertEquals("未接入", detail.displayItems.first { it.title == "getDtSessionId" }.value)
            assertEquals("未接入", detail.displayItems.first { it.title == "isBrowseMode" }.value)
            assertEquals("未接入", detail.displayItems.first { it.title == "isInReviewMode" }.value)
            assertEquals("未接入", detail.displayItems.first { it.title == "isTextMode" }.value)
            assertFalse(detail.displayItems.first { it.title == "getVersion" }.value.contains("未接入"))
            assertFalse(detail.displayItems.first { it.title == "getSystemFontScale" }.value.contains("未接入"))
        }
    }

    @Test
    fun reportSideEffectCasesAreNotMarkedPassWithoutObservableVerification() {
        val reportGroup = buildPlatformCapabilityTestGroups().first { it.key == "appReport" }
        val detail = reportGroup.detail as PlatformCapabilityTestListDetail

        val reportBeaconResult = detail.testCases.first { it.name == "reportBeacon" }.run()
        val reportDtResult = detail.testCases.first { it.name == "reportDt" }.run()

        assertTrue(reportBeaconResult.contains("NOOP"))
        assertTrue(reportDtResult.contains("NOOP"))
        assertFalse(reportBeaconResult.contains("ok"))
        assertFalse(reportDtResult.contains("ok"))
    }

    private fun withFakeAppStatus(block: () -> Unit) {
        val previous = QnPlatformLogic.appStatus
        QnPlatformLogic.appStatus = FakeAppStatus
        try {
            block()
        } finally {
            QnPlatformLogic.appStatus = previous
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private object FakeAppStatus : IAppStatus {
        override fun getVersion(): Int = 1
        override fun getVersionName(): String = "1.0.0"
        override fun getAppName(): String = "demo"
        override fun getAppBuildNo(): String = "1"
        override fun getQQAppId(): String = "qq"
        override fun getWxAppId(): String = "wx"
        override fun getDtSessionId(): String = "dt"
        override fun getQIMEI36(): String = "qimei"
        override fun getOAID(): String = "oaid"
        override fun getTOAID(): String = "toaid"
        override fun getTAID(): String = "taid"
        override fun getDevId(): String = "dev"
        override fun isDebug(): Boolean = true
        override fun isRdmDebug(): Boolean = false
        override fun isGrey(): Boolean = false
        override fun isIntegrationMode(): Boolean = true
        override fun isTalkbackEnabled(): Boolean = false
        override fun isBrowseMode(): Boolean = true
        override fun currentTextScaleGradient(): DensityScaleGradient = DensityScaleGradient.L1
        override fun isNightMode(): Boolean = false
        override fun isInReviewMode(): Boolean = true
        override fun isTextMode(): Boolean = true
        override fun setDarkMode(isDark: Boolean) = Unit
        override fun isSystemNightMode(): Boolean = false
        override fun isSupportFollowSystemBackgroundSetting(): Boolean = true
        override fun subscribeTheme(onThemeChanged: (isDark: Boolean) -> Unit) = Unit
        override fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double = 1.0
        override fun setScaleRatio(level: DensityScaleGradient) = Unit
        override fun getSystemFontScale(): Float = 1.0F
        override fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Double) -> Unit) = Unit
        override fun getDefaultFontFamily(): String = "system"
        override fun subscribeFontFamily(onFontFamilyChanged: (String) -> Unit) = Unit
        override fun getBottomBarHeight(): Int = 0
        override fun getNotificationAuthorizationStatus(
            guideConfigIfDenied: INotificationGuideConfig?,
            callback: (status: NotificationAuthorizationStatus) -> Unit
        ) {
            callback(NotificationAuthorizationStatus.Authorized)
        }
        override fun netState(): NetState = NetState.WIFI
        override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) = Unit
        override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) = Unit
        override fun getLaunchFrom(): String = SchemeFrom.ICON
        override fun getScreenWidth(): Int = 1
        override fun getScreenHeight(): Int = 1
        override fun getScreenWidthInch(): Float = 1F
        override fun getScreenHeightInch(): Float = 1F
        override fun getDpi(): Int = 1
        override fun getPackageName(): String = "pkg"
        override fun getPackageFirstInstallTime(): Long = 1L
        override fun getAppLaunchTimes(): Int = 1
        override fun getHardware(): String = "hardware"
        override fun getRomType(): String = "rom"
        override fun getStore(): String = "store"
        override fun getFixedStore(): String = "store"
        override fun enableSenor(): Boolean = true
        override fun getOsVs(): String = "os"
        override fun getTerm(): String = "term"
    }
}
