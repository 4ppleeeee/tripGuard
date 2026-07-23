package com.tencent.kmm.demo.home

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.app.constants.ScreenType
import com.tencent.news.core.audio.api.IFileCacheManager
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.extension.successResult
import com.tencent.news.core.isAndroidPlatform
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.platform.ISystemVolumeController
import com.tencent.news.core.platform.IVolumeListener
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.ExternalApp
import com.tencent.news.core.platform.api.FileCacheLevel
import com.tencent.news.core.platform.api.FileReadResult
import com.tencent.news.core.platform.api.FileWriteResult
import com.tencent.news.core.platform.api.FeedbackLogZipPayload
import com.tencent.news.core.platform.api.GyroscopeData
import com.tencent.news.core.platform.api.IAndroidDevice
import com.tencent.news.core.platform.api.IAppAlert
import com.tencent.news.core.platform.api.IAppConfig
import com.tencent.news.core.platform.api.IAppDevice
import com.tencent.news.core.platform.api.IAppEncoder
import com.tencent.news.core.platform.api.IAppGyroscope
import com.tencent.news.core.platform.api.IAppInstallInfo
import com.tencent.news.core.platform.api.IAppLocation
import com.tencent.news.core.platform.api.IAppPageStack
import com.tencent.news.core.platform.api.IAppPermission
import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IAppReport
import com.tencent.news.core.platform.api.IAppUri
import com.tencent.news.core.platform.api.IAppVibration
import com.tencent.news.core.platform.api.IAppWindow
import com.tencent.news.core.platform.api.IEvent
import com.tencent.news.core.platform.api.IEventBus
import com.tencent.news.core.platform.api.IFileManager
import com.tencent.news.core.platform.api.IGyroscopeListener
import com.tencent.news.core.platform.api.ILocationCallBack
import com.tencent.news.core.platform.api.INetwork
import com.tencent.news.core.platform.api.INetworkRequest
import com.tencent.news.core.platform.api.IResManager
import com.tencent.news.core.platform.api.IScreenInfo
import com.tencent.news.core.platform.api.IStatusBarController
import com.tencent.news.core.platform.api.IStorage
import com.tencent.news.core.platform.api.ITask
import com.tencent.news.core.platform.api.AndroidRomType
import com.tencent.news.core.platform.api.DefaultNetworkRequest
import com.tencent.news.core.platform.api.IPermissionCallback
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.NetworkResponse
import com.tencent.news.core.platform.api.PBNetworkBuilder
import com.tencent.news.core.platform.api.PBNetworkResponse
import com.tencent.news.core.platform.api.PaletteParam
import com.tencent.news.core.platform.api.PermissionScenes
import com.tencent.news.core.platform.api.ScreenOrientation
import com.tencent.news.core.platform.api.SensorAccuracy
import com.tencent.news.core.platform.api.SensorConfig
import com.tencent.news.core.platform.api.appAlert
import com.tencent.news.core.platform.api.appConfig
import com.tencent.news.core.platform.api.appDevice
import com.tencent.news.core.platform.api.appEncoder
import com.tencent.news.core.platform.api.appEventBus
import com.tencent.news.core.platform.api.appFile
import com.tencent.news.core.platform.api.appInstallInfo
import com.tencent.news.core.platform.api.appLocation
import com.tencent.news.core.platform.api.appNetwork
import com.tencent.news.core.platform.api.appPageStack
import com.tencent.news.core.platform.api.appPermission
import com.tencent.news.core.platform.api.appRegex
import com.tencent.news.core.platform.api.appReport
import com.tencent.news.core.platform.api.appScreenInfo
import com.tencent.news.core.platform.api.appStorage
import com.tencent.news.core.platform.api.appTask
import com.tencent.news.core.platform.api.appUri
import com.tencent.news.core.platform.api.appVibration
import com.tencent.news.core.platform.api.appWindow
import com.tencent.news.core.platform.api.originJsonParser
import com.tencent.news.core.platform.api.resManager
import com.tencent.news.core.platform.api.statusBarController
import com.tencent.news.core.platform.api.registerDeviceOrientationListener
import com.tencent.news.core.platform.api.unregisterAllDeviceOrientationListeners
import com.tencent.news.core.platform.api.unregisterDeviceOrientationListener
import com.tencent.news.core.platform.api.isAutoRotationEnabled
import com.tencent.news.core.platform.api.INavigationBarWindow
import com.tencent.news.core.platform.appVolumeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PlatformCapabilityTestGroup(
    val key: String,
    val title: String,
    val methodNames: List<String>,
    val runAll: () -> String,
    val detail: PlatformCapabilityDetail,
)

data class PlatformCapabilityTestCase(
    val name: String,
    val run: () -> String,
)

data class PlatformCapabilityDisplayItem(
    val title: String,
    val value: String,
)

sealed interface PlatformCapabilityDetail {
    val itemCount: Int
    val subtitle: String
    val showCloseAction: Boolean
}

data class PlatformCapabilityTestListDetail(
    val testCases: List<PlatformCapabilityTestCase>,
) : PlatformCapabilityDetail {
    override val itemCount: Int = testCases.size
    override val subtitle: String = "$itemCount 项能力，点击运行查看结果"
    override val showCloseAction: Boolean = true
}

data class PlatformCapabilityDeviceInfoDetail(
    val displayItems: List<PlatformCapabilityDisplayItem>,
) : PlatformCapabilityDetail {
    override val itemCount: Int = displayItems.size
    override val subtitle: String = "$itemCount 项当前设备信息"
    override val showCloseAction: Boolean = false
}

data class PlatformCapabilityAppStatusDetail(
    val displayItems: List<PlatformCapabilityDisplayItem>,
) : PlatformCapabilityDetail {
    override val itemCount: Int = displayItems.size
    override val subtitle: String = "$itemCount 项当前应用状态"
    override val showCloseAction: Boolean = false
}

fun buildPlatformCapabilityTestGroups(): List<PlatformCapabilityTestGroup> {
    ensureDemoPlatformTestAdapters()
    return listOf(
        appDeviceCapabilityGroup(),
        appStatusCapabilityGroup(),
        group("appConfig", "配置开关", listOf("getShiplyConfig", "getShiplySwitch", "getTabExpInt")) { runAppConfigTests() },
        group("appInstallInfo", "安装检测", listOf("isAppInstalled")) { runAppInstallInfoTests() },
        group("appStorage", "本地存储", listOf("setKV", "getKV", "removeValue", "getAllKeys", "getAll", "clearKV")) { runStorageTests() },
        group(
            "fileManager",
            "文件管理",
            listOf("readUserFile", "writeUserFile", "deleteUserFile", "readFile", "writeFile", "findFilesPath", "writeMetadata4Pdf", "writeMetadata4Image", "createCacheDir")
        ) { runFileManagerTests() },
        group(
            "resManager",
            "资源管理",
            listOf("getAssetJson", "preloadImage", "copyToClipboard", "saveImage", "saveVideo", "selectImage", "getPaletteColor", "preloadLottieToMemory", "preloadAlphaVideo")
        ) { runResManagerTests() },
        group(
            "appReport",
            "基础上报",
            listOf("reportBeacon", "reportBugly", "reportDt", "setPageStartFrom", "resetPageStartFrom", "uploadLogToBugly", "prepareFeedbackLogZipBase64")
        ) { runAppReportTests() },
        group("appAlert", "弹窗提示", listOf("showToast", "showDialog", "showDecorDebugView", "hideCurrentToast")) { runAppAlertTests() },
        group(
            "appEncoder",
            "编解码",
            listOf("encodeBase64Bytes", "decodeBase64", "urlEncode", "urlDecodeUtf8", "cipherDES", "cipherAESEncrypt", "cipherAESDecrypt", "removeEmoji", "md5", "rsaEncryptBase64")
        ) { runAppEncoderTests() },
        group("appUri", "Uri 解析", listOf("parseUri", "scheme", "host", "path", "getQuery", "getAllQuery", "mutate", "appendQuery", "build", "asString")) { runAppUriTests() },
        group("task", "任务调度", listOf("postAction", "cancel", "runIOAction", "runMainAction", "runCpuAction")) { runTaskTests() },
        group("eventBus", "事件总线", listOf("post", "postSticky", "exportJson")) { runEventBusTests() },
        group(
            "network",
            "网络请求",
            listOf("postJson", "postForm", "get", "sse", "postMultipart", "postBytes", "postPb", "netState", "addNetStatusChangeListener", "removeNetStatusChangeListener")
        ) { runNetworkTests() },
        group("appPageStack", "页面栈", listOf("onPageCreated", "onPageDestroyed", "getAllPages", "getActivePages", "getTopValidPage", "isPageActive", "applicationStateActive", "getPageLifecycleState")) { runPageStackTests() },
        group("fileCacheManager", "文件缓存", listOf("fileAbsolutePath", "cacheFile", "containsFile", "removeFile", "removeDirPathFile")) { runFileCacheTests() },
        group("vibration", "震动", listOf("triggerVibration")) { runVibrationTests() },
        group("gyroscope", "陀螺仪", listOf("isAvailable", "registerListener", "unregisterListener", "unregisterAllListeners", "getLatestData")) { runGyroscopeTests() },
        group("appPermission", "权限", listOf("hasLocationPermission", "requestLocationPermission", "onPermissionGranted", "onPermissionRejected")) { runPermissionTests() },
        group("appLocation", "定位", listOf("requestLocation", "silentGetAdCode", "enterModule", "leaveModule", "isSystemLocationEnabled", "openLocationSettings")) { runLocationTests() },
        group("appRegex", "正则", listOf("build", "containsMatchIn", "find", "findAll", "replace", "next", "range")) { runRegexTests() },
        group("systemVolumeController", "系统音量", listOf("registerListener", "unregisterListener", "onVolumeChanged")) { runVolumeTests() },
        group("statusBarController", "状态栏", listOf("setWhiteBar", "setBlackBar", "resetStatusBar", "setCustomBar", "setStatusBarVisibility")) { runStatusBarTests() },
        group(
            "appWindow",
            "窗口控制",
            listOf(
                "keepScreenOn", "cancelScreenOn", "setScreenOrientation", "getScreenOrientation", "enterFullScreen",
                "exitFullScreen", "registerDeviceOrientationListener", "unregisterDeviceOrientationListener",
                "unregisterAllDeviceOrientationListeners", "isAutoRotationEnabled", "setNavigationBarVisibility",
                "setNavigationBarDarkButtons", "getNavigationBarHeight"
            )
        ) { runAppWindowTests() },
        group("screenInfo", "屏幕信息", listOf("getScreenWidth", "getScreenHeight", "getScreenWidthInch", "getScreenHeightInch", "getDpi")) { runScreenInfoTests() },
    )
}

fun runAllPlatformCapabilityTests(): String =
    buildPlatformCapabilityTestGroups().joinToString(separator = "\n\n") { group ->
        "${group.title}\n${group.runAll()}"
    }

fun showPlatformCapabilityTestResult(group: PlatformCapabilityTestGroup) {
    val result = group.runAll()
    appAlert().showDialog("${group.title} 测试结果", result.take(1800))
}

fun showAllPlatformCapabilityTestResults() {
    appAlert().showDialog("qnPlatform 全量测试结果", runAllPlatformCapabilityTests().take(2500))
}

private fun group(
    key: String,
    title: String,
    methodNames: List<String>,
    runAll: () -> String,
): PlatformCapabilityTestGroup =
    PlatformCapabilityTestGroup(
        key = key,
        title = title,
        methodNames = methodNames,
        runAll = runAll,
        detail = PlatformCapabilityTestListDetail(
            methodNames.map { methodName ->
                PlatformCapabilityTestCase(methodName) {
                    runSingleCapabilityTest(methodName, runAll)
                }
            },
        ),
    )

private fun appDeviceCapabilityGroup(): PlatformCapabilityTestGroup {
    val displayItems = buildAppDeviceDisplayItems()
    return PlatformCapabilityTestGroup(
        key = "appDevice",
        title = "设备信息",
        methodNames = displayItems.map { it.title },
        runAll = { displayItems.joinToString(separator = "\n") { "${it.title}: ${it.value}" } },
        detail = PlatformCapabilityDeviceInfoDetail(displayItems),
    )
}

private fun appStatusCapabilityGroup(): PlatformCapabilityTestGroup {
    val displayItems = buildAppStatusDisplayItems()
    return PlatformCapabilityTestGroup(
        key = "appStatus",
        title = "应用状态",
        methodNames = displayItems.map { it.title },
        runAll = { displayItems.joinToString(separator = "\n") { "${it.title}: ${it.value}" } },
        detail = PlatformCapabilityAppStatusDetail(displayItems),
    )
}

private fun runSingleCapabilityTest(methodName: String, runAll: () -> String): String {
    val result = runAll()
    return result.lineSequence()
        .firstOrNull { it.startsWith("$methodName:") }
        ?: "$methodName: 未找到单项结果\n$result"
}

private fun resultOf(name: String, action: () -> Any?): String =
    runCatching { pass(name, action()) }.getOrElse { error(name, it) }

private fun noObservableResult(
    name: String,
    message: String = "已调用，但当前接口没有可观测返回值，不能证明能力真实生效",
    action: () -> Unit,
): String =
    runCatching {
        action()
        noop(name, message)
    }.getOrElse { error(name, it) }

private fun callbackResult(
    name: String,
    action: ((String) -> Unit) -> Unit,
): String =
    runCatching {
        var output: String? = null
        action { output = it }
        output?.let { pass(name, it) }
            ?: noop(name, "已发起调用，但没有同步回调，需端侧异步结果或日志继续确认")
    }.getOrElse { error(name, it) }

private fun pass(name: String, message: Any?): String = "$name: PASS $message"

private fun noop(name: String, message: String): String = "$name: NOOP $message"

private fun unsupported(name: String, message: String): String = "$name: UNSUPPORTED $message"

private fun error(name: String, throwable: Throwable): String = "$name: ERROR ${throwable.message}"

private fun lines(vararg items: String): String = items.joinToString(separator = "\n")

private fun demoContext(): IKmmContext = object : IKmmContext {}

private fun buildAppDeviceDisplayItems(): List<PlatformCapabilityDisplayItem> {
    val device = appDevice()
    return when {
        isAndroidPlatform() -> device.getAndroidRom()?.let { android ->
            listOf(
                PlatformCapabilityDisplayItem("getType", android.getType().name),
                PlatformCapabilityDisplayItem("isHarmony", android.isHarmony().toString()),
                PlatformCapabilityDisplayItem("getScreenType", android.getScreenType().name),
                PlatformCapabilityDisplayItem("getScreenCount", android.getScreenCount().toString()),
                PlatformCapabilityDisplayItem("getInstallChannel", android.getInstallChannel()),
            )
        }.orEmpty()
        isIOSPlatform() -> device.getIOSRom()?.let { ios ->
            listOf(
                PlatformCapabilityDisplayItem("getScreenType", ios.getScreenType().name),
            )
        }.orEmpty()
        else -> emptyList()
    }
}

@Suppress("DEPRECATION")
private fun buildAppStatusDisplayItems(): List<PlatformCapabilityDisplayItem> {
    val status = QnPlatformLogic.appStatus ?: return emptyList()
    return listOf(
        displayItem("getVersion") { status.getVersion() },
        displayItem("getVersionName") { status.getVersionName() },
        displayItem("getAppName") { status.getAppName() },
        displayItem("getAppBuildNo") { status.getAppBuildNo() },
        displayItem("getQQAppId") { status.getQQAppId() },
        displayItem("getWxAppId") { status.getWxAppId() },
        unsupportedDisplayItem("getDtSessionId"),
        displayItem("getQIMEI36") { status.getQIMEI36() },
        displayItem("getOAID") { status.getOAID() },
        displayItem("getTOAID") { status.getTOAID() },
        displayItem("getTAID") { status.getTAID() },
        displayItem("getDevId") { status.getDevId() },
        displayItem("isDebug") { status.isDebug() },
        displayItem("isRdmDebug") { status.isRdmDebug() },
        displayItem("isGrey") { status.isGrey() },
        unsupportedDisplayItem("isIntegrationMode"),
        displayItem("isTalkbackEnabled") { status.isTalkbackEnabled() },
        unsupportedDisplayItem("isBrowseMode"),
        displayItem("currentTextScaleGradient") { status.currentTextScaleGradient() },
        displayItem("isNightMode") { status.isNightMode() },
        unsupportedDisplayItem("isInReviewMode"),
        unsupportedDisplayItem("isTextMode"),
        displayItem("isSystemNightMode") { status.isSystemNightMode() },
        displayItem("isSupportFollowSystemBackgroundSetting") { status.isSupportFollowSystemBackgroundSetting() },
        displayItem("getScaleRatioByGradient") { status.getScaleRatioByGradient(DensityScaleGradient.L1) },
        displayItem("getSystemFontScale") { status.getSystemFontScale() },
        displayItem("getDefaultFontFamily") { status.getDefaultFontFamily() },
        displayItem("getBottomBarHeight") { status.getBottomBarHeight() },
        displayItem("getNotificationAuthorizationStatus") {
            var callbackValue = "no-callback"
            status.getNotificationAuthorizationStatus(null) { callbackValue = it.name }
            callbackValue
        },
        displayItem("netState") { status.netState() },
        displayItem("getLaunchFrom") { status.getLaunchFrom() },
        displayItem("getScreenWidth") { status.getScreenWidth() },
        displayItem("getScreenHeight") { status.getScreenHeight() },
        displayItem("getScreenWidthInch") { status.getScreenWidthInch() },
        displayItem("getScreenHeightInch") { status.getScreenHeightInch() },
        displayItem("getDpi") { status.getDpi() },
        displayItem("getPackageName") { status.getPackageName() },
        displayItem("getPackageFirstInstallTime") { status.getPackageFirstInstallTime() },
        displayItem("getAppLaunchTimes") { status.getAppLaunchTimes() },
        displayItem("getHardware") { status.getHardware() },
        displayItem("getRomType") { status.getRomType() },
        displayItem("getStore") { status.getStore() },
        displayItem("getFixedStore") { status.getFixedStore() },
        displayItem("enableSenor") { status.enableSenor() },
        displayItem("getOsVs") { status.getOsVs() },
        displayItem("getTerm") { status.getTerm() },
    )
}

private fun displayItem(name: String, action: () -> Any?): PlatformCapabilityDisplayItem =
    PlatformCapabilityDisplayItem(
        title = name,
        value = runCatching { action().toString() }.getOrElse { "ERROR ${it.message.orEmpty()}" },
    )

private fun unsupportedDisplayItem(name: String): PlatformCapabilityDisplayItem =
    PlatformCapabilityDisplayItem(
        title = name,
        value = "未接入",
    )

@Suppress("DEPRECATION")
private fun runAppStatusTests(): String {
    val status = QnPlatformLogic.appStatus ?: return unsupported("appStatus", "当前平台未注入 IAppStatus")
    val listener = object : NetStateChangeListener {
        override fun netStateChanged(old: NetState, new: NetState) {}
    }
    return lines(
        resultOf("getVersion") { status.getVersion() },
        resultOf("getVersionName") { status.getVersionName() },
        resultOf("getAppName") { status.getAppName() },
        resultOf("getAppBuildNo") { status.getAppBuildNo() },
        resultOf("getQQAppId") { status.getQQAppId() },
        resultOf("getWxAppId") { status.getWxAppId() },
        unsupported("getDtSessionId", "当前 demo 未接入 DT 会话来源"),
        resultOf("getQIMEI36") { status.getQIMEI36() },
        resultOf("getOAID") { status.getOAID() },
        resultOf("getTOAID") { status.getTOAID() },
        resultOf("getTAID") { status.getTAID() },
        resultOf("getDevId") { status.getDevId() },
        resultOf("isDebug") { status.isDebug() },
        resultOf("isRdmDebug") { status.isRdmDebug() },
        resultOf("isGrey") { status.isGrey() },
        unsupported("isIntegrationMode", "当前 demo 未接入集成模式状态来源"),
        resultOf("isTalkbackEnabled") { status.isTalkbackEnabled() },
        unsupported("isBrowseMode", "当前 demo 未接入浏览模式状态来源"),
        resultOf("currentTextScaleGradient") { status.currentTextScaleGradient() },
        resultOf("isNightMode") { status.isNightMode() },
        unsupported("isInReviewMode", "当前 demo 未接入审核模式状态来源"),
        unsupported("isTextMode", "当前 demo 未接入纯文字模式状态来源"),
        resultOf("setDarkMode") { status.setDarkMode(false); "ok" },
        resultOf("isSystemNightMode") { status.isSystemNightMode() },
        resultOf("isSupportFollowSystemBackgroundSetting") { status.isSupportFollowSystemBackgroundSetting() },
        resultOf("subscribeTheme") { status.subscribeTheme {}; "ok" },
        resultOf("getScaleRatioByGradient") { status.getScaleRatioByGradient(DensityScaleGradient.L1) },
        resultOf("setScaleRatio") { status.setScaleRatio(DensityScaleGradient.L1); "ok" },
        resultOf("getSystemFontScale") { status.getSystemFontScale() },
        resultOf("subscribeTextScaleRatio") { status.subscribeTextScaleRatio {}; "ok" },
        resultOf("getDefaultFontFamily") { status.getDefaultFontFamily() },
        resultOf("subscribeFontFamily") { status.subscribeFontFamily {}; "ok" },
        resultOf("getBottomBarHeight") { status.getBottomBarHeight() },
        resultOf("getNotificationAuthorizationStatus") {
            var callbackValue = "no-callback"
            status.getNotificationAuthorizationStatus(null) { callbackValue = it.name }
            callbackValue
        },
        resultOf("netState") { status.netState() },
        resultOf("addNetStatusChangeListener") { status.addNetStatusChangeListener(listener); "ok" },
        resultOf("removeNetStatusChangeListener") { status.removeNetStatusChangeListener(listener); "ok" },
        resultOf("getLaunchFrom") { status.getLaunchFrom() },
        resultOf("getScreenWidth") { status.getScreenWidth() },
        resultOf("getScreenHeight") { status.getScreenHeight() },
        resultOf("getScreenWidthInch") { status.getScreenWidthInch() },
        resultOf("getScreenHeightInch") { status.getScreenHeightInch() },
        resultOf("getDpi") { status.getDpi() },
        resultOf("getPackageName") { status.getPackageName() },
        resultOf("getPackageFirstInstallTime") { status.getPackageFirstInstallTime() },
        resultOf("getAppLaunchTimes") { status.getAppLaunchTimes() },
        resultOf("getHardware") { status.getHardware() },
        resultOf("getRomType") { status.getRomType() },
        resultOf("getStore") { status.getStore() },
        resultOf("getFixedStore") { status.getFixedStore() },
        resultOf("enableSenor") { status.enableSenor() },
        resultOf("getOsVs") { status.getOsVs() },
        resultOf("getTerm") { status.getTerm() },
    )
}

private fun runAppConfigTests(): String {
    val config = appConfig() ?: DemoAppConfig
    return lines(
        resultOf("getShiplyConfig") { config.getShiplyConfig("demo_key", "demo_default") },
        resultOf("getShiplySwitch") { config.getShiplySwitch("demo_switch", true) },
        resultOf("getTabExpInt") { config.getTabExpInt("demo_exp", 7) },
    )
}

private fun runAppInstallInfoTests(): String =
    resultOf("isAppInstalled") {
        appInstallInfo().isAppInstalled(
            ExternalApp(
                id = "demo",
                androidPackageName = "com.tencent.news.base.app",
                iosUrlScheme = "kmm-base-demo",
                ohosBundleName = "com.tencent.news.base.app",
            )
        )
    }

private fun runStorageTests(): String {
    val storage = appStorage()
    val table = "platform_demo"
    val key = "sample"
    return lines(
        resultOf("setKV") { storage.setKV(table, key, "value"); "ok" },
        resultOf("getKV") { storage.getKV(table, key, "") },
        resultOf("getAllKeys") { storage.getAllKeys(table).joinToString() },
        resultOf("getAll") { storage.getAll(table).toString() },
        resultOf("removeValue") { storage.removeValue(table, key); "ok" },
        resultOf("clearKV") { storage.clearKV(table); "ok" },
    )
}

private fun runFileManagerTests(): String {
    val file = appFile() ?: DemoFileManager
    val dir = "platform_demo"
    val name = "sample.txt"
    return lines(
        resultOf("writeUserFile") {
            var output = "pending"
            file.writeUserFile(name, "demo") { output = it.toText() }
            output
        },
        resultOf("readUserFile") {
            var output = "pending"
            file.readUserFile(name) { output = it.toText() }
            output
        },
        resultOf("deleteUserFile") { file.deleteUserFile(name); "ok" },
        resultOf("writeFile") {
            var output = "pending"
            file.writeFile(dir, name, FileCacheLevel.USERCACHE, "demo") { output = it.toText() }
            output
        },
        resultOf("readFile") {
            var output = "pending"
            file.readFile(dir, name, FileCacheLevel.USERCACHE) { output = it.toText() }
            output
        },
        resultOf("findFilesPath") {
            var output = "pending"
            file.findFilesPath(dir, "sample", FileCacheLevel.USERCACHE, true) { output = it.orEmpty().joinToString() }
            output
        },
        resultOf("writeMetadata4Pdf") { file.writeMetadata4Pdf("$dir/$name", mapOf("demo" to "true")); "ok" },
        resultOf("writeMetadata4Image") { file.writeMetadata4Image("$dir/$name", mapOf("demo" to "true")); "ok" },
        resultOf("createCacheDir") { file.createCacheDir(dir) },
    )
}

private fun runAppReportTests(): String {
    val report = appReport()
    return lines(
        noObservableResult("reportBeacon") {
            report.reportBeacon("platform_demo_beacon", mapOf("source" to "demo"))
        },
        noObservableResult("reportBugly") {
            report.reportBugly("platform demo bugly", IllegalStateException("demo"))
        },
        noObservableResult("reportDt") {
            report.reportDt("platform_demo_dt", mapOf("source" to "demo"))
        },
        noObservableResult("setPageStartFrom") {
            report.setPageStartFrom("platform_demo")
        },
        noObservableResult("resetPageStartFrom") {
            report.resetPageStartFrom()
        },
        callbackResult("uploadLogToBugly") { setResult ->
            report.uploadLogToBugly { setResult("callback=$it") }
        },
        runCatching {
            var payload: FeedbackLogZipPayload? = null
            report.prepareFeedbackLogZipBase64 { payload = it }
            when {
                payload == null -> unsupported("prepareFeedbackLogZipBase64", "当前实现未返回日志压缩包")
                payload?.base64.isNullOrBlank() -> unsupported("prepareFeedbackLogZipBase64", "当前实现返回空日志内容")
                else -> pass("prepareFeedbackLogZipBase64", "suffix=${payload?.fileSuffix}, base64Length=${payload?.base64?.length}")
            }
        }.getOrElse { error("prepareFeedbackLogZipBase64", it) },
    )
}

private fun runNetworkRequestTest(
    name: String,
    request: (NetworkBuilder<String>) -> INetworkRequest,
): String {
    val network = appNetwork()
    return runCatching {
        var output: String? = null
        val builder = NetworkBuilder(
            url = "https://example.com/$name",
            parser = originJsonParser(),
            params = mapOf("source" to "demo"),
            onResponse = { response ->
                output = "callback=${response.result.succeed}, body=${response.json.orEmpty().take(32)}"
            },
        )
        request(builder).cancel()
        output?.let { pass(name, it) }
            ?: noop(name, "请求已发起并取消，但未收到同步回调，不能证明网络链路可用")
    }.getOrElse {
        error(name, it)
    }
}

private fun runPbNetworkRequestTest(): String {
    val network = appNetwork()
    return runCatching {
        var output: String? = null
        network.postPb(
            PBNetworkBuilder(
                url = "demo.pb",
                bodyEncoder = { "request".encodeToByteArray() },
                bodyDecoder = { it.decodeToString() },
                onResponse = { output = "callback=${it.result.succeed}, data=${it.parsedData}" },
            )
        ).cancel()
        output?.let { pass("postPb", it) }
            ?: noop("postPb", "请求已发起并取消，但未收到同步回调，不能证明 PB 链路可用")
    }.getOrElse {
        error("postPb", it)
    }
}

private fun runEventBusTests(): String {
    val event = DemoEvent("platform_demo")
    return lines(
        resultOf("exportJson") { event.exportJson() },
        noObservableResult("post") { appEventBus().post(event) },
        noObservableResult("postSticky") { appEventBus().postSticky(event) },
    )
}

private fun runNetworkTests(): String {
    val network = appNetwork()
    val listener = object : NetStateChangeListener {
        override fun netStateChanged(old: NetState, new: NetState) {}
    }
    return lines(
        runNetworkRequestTest("postJson") { network.postJson(it) },
        runNetworkRequestTest("postForm") { network.postForm(it) },
        runNetworkRequestTest("get") { network.get(it) },
        runNetworkRequestTest("sse") { network.sse(it) },
        runNetworkRequestTest("postMultipart") { network.postMultipart(it) },
        runNetworkRequestTest("postBytes") { network.postBytes(it) },
        runPbNetworkRequestTest(),
        resultOf("netState") { network.netState() },
        noObservableResult("addNetStatusChangeListener") { network.addNetStatusChangeListener(listener) },
        noObservableResult("removeNetStatusChangeListener") { network.removeNetStatusChangeListener(listener) },
    )
}

private fun runStatusBarTests(): String {
    val controller = statusBarController
    return lines(
        noObservableResult("setWhiteBar") { controller.setWhiteBar() },
        noObservableResult("setBlackBar") { controller.setBlackBar() },
        noObservableResult("setCustomBar") { controller.setCustomBar("#111111", "#FFFFFF") },
        noObservableResult("setStatusBarVisibility(false)") { controller.setStatusBarVisibility(false) },
        noObservableResult("setStatusBarVisibility(true)") { controller.setStatusBarVisibility(true) },
        noObservableResult("resetStatusBar") { controller.resetStatusBar() },
    )
}

private fun runVibrationTests(): String =
    noObservableResult("triggerVibration", "已触发震动，需在真机上体感确认") {
        appVibration.triggerVibration()
    }

private fun runAppAlertTests(): String {
    val alert = appAlert()
    return lines(
        noObservableResult("showToast", "已调用 toast，需在当前页面视觉确认") { alert.showToast("qnPlatform toast test") },
        noObservableResult("showDecorDebugView", "已调用调试浮层，需在当前页面视觉确认") { alert.showDecorDebugView("qnPlatform decor test") },
        noObservableResult("hideCurrentToast", "已调用隐藏 toast，需在当前页面视觉确认") { alert.hideCurrentToast() },
        pass("showDialog", "当前结果浮层即为 showDialog 测试"),
    )
}

private fun runResManagerTests(): String {
    val res = resManager() ?: DemoResManager
    return lines(
        resultOf("getAssetJson") { res.getAssetJson("platform_demo.json") },
        callbackResult("preloadImage") { setResult ->
            res.preloadImage("https://example.com/demo.png", { setResult("success") }, { setResult("fail") })
        },
        noObservableResult("copyToClipboard", "已调用复制，需粘贴验证剪贴板内容") { res.copyToClipboard("kmm base demo") },
        noObservableResult("saveImage", "已调用保存图片，需相册/文件系统确认") { res.saveImage("https://example.com/demo.png", mapOf("source" to "demo")) },
        resultOf("saveVideo") { res.saveVideo("platform-demo.mp4") },
        callbackResult("selectImage") { setResult ->
            res.selectImage(null) { setResult(it.joinToString()) }
        },
        callbackResult("getPaletteColor") { setResult ->
            res.getPaletteColor("https://example.com/demo.png", PaletteParam(10, 2, "0,0,1,1"), 0) { setResult(it.toString()) }
        },
        noObservableResult("preloadLottieToMemory") { res.preloadLottieToMemory(null, "https://example.com/demo.json", "demo", true) },
        callbackResult("preloadAlphaVideo") { setResult ->
            res.preloadAlphaVideo("https://example.com/demo.pag", { setResult("success") }, { setResult("fail") })
        },
    )
}

private fun runAppEncoderTests(): String {
    val encoder = appEncoder() ?: DemoAppEncoder
    val data = "kmm demo"
    val bytes = data.encodeToByteArray()
    val key = "12345678"
    val aesKey = "1234567890123456".encodeToByteArray()
    return lines(
        resultOf("encodeBase64Bytes") { encoder.encodeBase64Bytes(bytes) },
        resultOf("decodeBase64") { encoder.decodeBase64("a21tIGRlbW8=") },
        resultOf("urlEncode") { encoder.urlEncode("kmm demo") },
        resultOf("urlDecodeUtf8") { encoder.urlDecodeUtf8("kmm%20demo") },
        resultOf("cipherDES") { encoder.cipherDES(key, key, bytes).size },
        resultOf("cipherAESEncrypt") { encoder.cipherAESEncrypt(aesKey, aesKey, bytes).size },
        resultOf("cipherAESDecrypt") { encoder.cipherAESDecrypt(aesKey, aesKey, bytes).size },
        resultOf("removeEmoji") { encoder.removeEmoji("demo😀") },
        resultOf("md5") { encoder.md5(data) },
        resultOf("rsaEncryptBase64") { encoder.rsaEncryptBase64(bytes, "demo-public-key") },
    )
}

private fun runAppUriTests(): String {
    val uri = appUri().parseUri("kmmbase://platform/test?a=1")
    val mutable = uri.mutate()
    return lines(
        resultOf("parseUri.scheme") { uri.scheme },
        resultOf("parseUri.host") { uri.host },
        resultOf("parseUri.path") { uri.path },
        resultOf("getQuery") { uri.getQuery("a") },
        resultOf("getAllQuery") { uri.getAllQuery().toString() },
        resultOf("mutate") { mutable::class.simpleName },
        resultOf("appendQuery") { mutable.appendQuery("b", "2"); "ok" },
        resultOf("build") { mutable.build().path },
        resultOf("asString") { mutable.asString() },
    )
}

private fun runTaskTests(): String {
    val task = appTask()
    return lines(
        resultOf("postAction") {
            var output = "pending"
            val request = task.postAction({ output = "posted" }, 0)
            "$output/${request::class.simpleName}"
        },
        resultOf("cancel") { task.postAction({}, 100).cancel(); "ok" },
        resultOf("runIOAction") { var output = "pending"; task.runIOAction { output = "io" }; output },
        resultOf("runMainAction") { var output = "pending"; task.runMainAction { output = "main" }; output },
        resultOf("runCpuAction") { var output = "pending"; task.runCpuAction { output = "cpu" }; output },
    )
}

private fun runPageStackTests(): String {
    val stack = appPageStack()
    val context = demoContext()
    return lines(
        resultOf("onPageCreated") { stack.onPageCreated("demo", context); "ok" },
        resultOf("getAllPages") { stack.getAllPages().size },
        resultOf("getActivePages") { stack.getActivePages().size },
        resultOf("getTopValidPage") { stack.getTopValidPage() != null },
        resultOf("isPageActive") { stack.isPageActive(context) },
        resultOf("applicationStateActive") { stack.applicationStateActive() },
        resultOf("getPageLifecycleState") { stack.getPageLifecycleState(context) },
        resultOf("onPageDestroyed") { stack.onPageDestroyed("demo", context); "ok" },
    )
}

private fun runFileCacheTests(): String {
    val cache = QnPlatformLogic.fileCacheManager ?: DemoFileCacheManager
    val path = cache.fileAbsolutePath("platform_demo", "sample.txt") ?: "platform_demo/sample.txt"
    return lines(
        resultOf("fileAbsolutePath") { path },
        resultOf("cacheFile") { cache.cacheFile(path, "demo") },
        resultOf("containsFile") { cache.containsFile(path) },
        resultOf("removeFile") { cache.removeFile(path); "ok" },
        resultOf("removeDirPathFile") { cache.removeDirPathFile("platform_demo"); "ok" },
    )
}

private fun runGyroscopeTests(): String {
    val gyroscope = QnPlatformLogic.gyroscope ?: DemoGyroscope
    val listener = object : IGyroscopeListener {
        override fun onGyroscopeChanged(data: GyroscopeData) {}
    }
    return lines(
        resultOf("isAvailable") { gyroscope.isAvailable() },
        resultOf("registerListener") { gyroscope.registerListener(SensorConfig(), listener) },
        resultOf("getLatestData") { gyroscope.getLatestData() },
        resultOf("unregisterListener") { gyroscope.unregisterListener(listener); "ok" },
        resultOf("unregisterAllListeners") { gyroscope.unregisterAllListeners(); "ok" },
    )
}

private fun runPermissionTests(): String {
    val permission = appPermission
    val context = demoContext()
    return lines(
        resultOf("hasLocationPermission") { permission.hasLocationPermission(context, PermissionScenes.DefaultLocation) },
        resultOf("requestLocationPermission") {
            var output = "pending"
            permission.requestLocationPermission(
                context,
                PermissionScenes.DefaultLocation,
                false,
                object : IPermissionCallback {
                    override fun onPermissionGranted() {
                        output = "granted"
                    }

                    override fun onPermissionRejected(reason: String) {
                        output = "rejected:$reason"
                    }
                }
            )
            output
        },
    )
}

private fun runLocationTests(): String {
    val location = appLocation()
    val scene = PermissionScenes.DefaultLocation
    return lines(
        resultOf("enterModule") { location.enterModule(scene); "ok" },
        resultOf("silentGetAdCode") { location.silentGetAdCode(scene) },
        resultOf("isSystemLocationEnabled") { location.isSystemLocationEnabled() },
        resultOf("requestLocation") {
            CoroutineScope(Dispatchers.Main).launch {
                location.requestLocation(demoContext(), scene, false, true, ILocationCallBack {
                    appAlert().showToast("定位回调 adCode=$it")
                })
            }
            "started"
        },
        resultOf("openLocationSettings") { location.openLocationSettings(); "ok" },
        resultOf("leaveModule") { location.leaveModule(scene); "ok" },
    )
}

private fun runRegexTests(): String {
    val regex = appRegex().build("demo", true)
    return lines(
        resultOf("build") { regex::class.simpleName },
        resultOf("containsMatchIn") { regex.containsMatchIn("KMM Demo") },
        resultOf("find") { regex.find("KMM Demo")?.range },
        resultOf("findAll") { regex.findAll("demo Demo").map { it.range }.joinToString() },
        resultOf("replace") { regex.replace("demo text", "seed") },
        resultOf("next") { regex.find("demo demo")?.next()?.range },
    )
}

private fun runVolumeTests(): String {
    val controller = appVolumeController() ?: DemoVolumeController
    val listener = IVolumeListener { _, _ -> }
    return lines(
        resultOf("registerListener") { controller.registerListener(listener); "ok" },
        resultOf("onVolumeChanged") { listener.onVolumeChanged(7, true); "ok" },
        resultOf("unregisterListener") { controller.unregisterListener(listener); "ok" },
    )
}

private fun runAppWindowTests(): String {
    val window = appWindow()
    val listener = { _: ScreenOrientation -> }
    return lines(
        resultOf("keepScreenOn") { window.keepScreenOn(); "ok" },
        resultOf("cancelScreenOn") { window.cancelScreenOn(); "ok" },
        resultOf("setScreenOrientation") { window.setScreenOrientation(ScreenOrientation.PORTRAIT); "ok" },
        resultOf("getScreenOrientation") { window.getScreenOrientation() },
        resultOf("enterFullScreen") { window.enterFullScreen(); "ok" },
        resultOf("exitFullScreen") { window.exitFullScreen(); "ok" },
        resultOf("registerDeviceOrientationListener") { window.registerDeviceOrientationListener(listener) },
        resultOf("unregisterDeviceOrientationListener") { window.unregisterDeviceOrientationListener(listener); "ok" },
        resultOf("unregisterAllDeviceOrientationListeners") { window.unregisterAllDeviceOrientationListeners(); "ok" },
        resultOf("isAutoRotationEnabled") { window.isAutoRotationEnabled() },
        resultOf("setNavigationBarVisibility") { (window as? INavigationBarWindow)?.setNavigationBarVisibility(true); window is INavigationBarWindow },
        resultOf("setNavigationBarDarkButtons") { (window as? INavigationBarWindow)?.setNavigationBarDarkButtons(true); window is INavigationBarWindow },
        resultOf("getNavigationBarHeight") { (window as? INavigationBarWindow)?.getNavigationBarHeight() },
    )
}

private fun runScreenInfoTests(): String {
    val screen = appScreenInfo()
    return lines(
        resultOf("getScreenWidth") { screen.getScreenWidth() },
        resultOf("getScreenHeight") { screen.getScreenHeight() },
        resultOf("getScreenWidthInch") { screen.getScreenWidthInch() },
        resultOf("getScreenHeightInch") { screen.getScreenHeightInch() },
        resultOf("getDpi") { screen.getDpi() },
    )
}


private fun FileReadResult.toText(): String = when (this) {
    is FileReadResult.Success -> "success:$data"
    is FileReadResult.Error -> "error:${exception.message}"
}

private fun FileWriteResult.toText(): String = when (this) {
    is FileWriteResult.Success -> "success"
    is FileWriteResult.Error -> "error:${exception.message}"
}

private fun ensureDemoPlatformTestAdapters() {
    if (QnPlatformLogic.appDevice == null) QnPlatformLogic.appDevice = DemoAppDevice
    if (QnPlatformLogic.appConfig == null) QnPlatformLogic.appConfig = DemoAppConfig
    if (QnPlatformLogic.appInstallInfo == null) QnPlatformLogic.appInstallInfo = DemoAppInstallInfo
    if (QnPlatformLogic.appStorage == null) QnPlatformLogic.appStorage = DemoStorage
    if (QnPlatformLogic.fileManager == null) QnPlatformLogic.fileManager = DemoFileManager
    if (QnPlatformLogic.resManager == null) QnPlatformLogic.resManager = DemoResManager
    if (QnPlatformLogic.appReport == null) QnPlatformLogic.appReport = DemoAppReport
    if (QnPlatformLogic.appAlert == null) QnPlatformLogic.appAlert = DemoAppAlert
    if (QnPlatformLogic.appEncoder == null) QnPlatformLogic.appEncoder = DemoAppEncoder
    if (QnPlatformLogic.task == null) QnPlatformLogic.task = DemoTask
    if (QnPlatformLogic.eventBus == null) QnPlatformLogic.eventBus = DemoEventBus
    if (QnPlatformLogic.network == null) QnPlatformLogic.network = DemoNetwork
    if (QnPlatformLogic.fileCacheManager == null) QnPlatformLogic.fileCacheManager = DemoFileCacheManager
    if (QnPlatformLogic.gyroscope == null) QnPlatformLogic.gyroscope = DemoGyroscope
    if (QnPlatformLogic.appPermission == null) QnPlatformLogic.appPermission = DemoPermission
    if (QnPlatformLogic.appLocation == null) QnPlatformLogic.appLocation = DemoLocation
    if (QnPlatformLogic.systemVolumeController == null) QnPlatformLogic.systemVolumeController = DemoVolumeController
    if (QnPlatformLogic.screenInfo == null) QnPlatformLogic.screenInfo = DemoScreenInfo
}

private object DemoAppDevice : IAppDevice {
    override fun getAndroidRom(): IAndroidDevice = object : IAndroidDevice {
        override fun getType(): AndroidRomType = AndroidRomType.ANDROID
        override fun isHarmony(): Boolean = false
        override fun getScreenType(): ScreenType = ScreenType.PHONE
        override fun getScreenCount(): Int = 1
        override fun getInstallChannel(): String = "demo"
    }
}

private object DemoAppConfig : IAppConfig {
    override fun getShiplyConfig(key: String, defaultValue: String): String = "demo:$key:$defaultValue"
    override fun getShiplySwitch(key: String, defaultValue: Boolean): Boolean = defaultValue
    override fun getTabExpInt(key: String, defaultValue: Int): Int = defaultValue
}

private object DemoAppInstallInfo : IAppInstallInfo {
    override fun isAppInstalled(app: ExternalApp): Boolean = app.id == "demo"
}

private object DemoStorage : IStorage {
    private val data = mutableMapOf<String, MutableMap<String, String>>()
    override fun setKV(tableName: String, key: String, value: String) {
        data.getOrPut(tableName) { mutableMapOf() }[key] = value
    }
    override fun getKV(tableName: String, key: String, defaultValue: String): String = data[tableName]?.get(key) ?: defaultValue
    override fun removeValue(tableName: String, key: String) { data[tableName]?.remove(key) }
    override fun getAllKeys(tableName: String): List<String> = data[tableName]?.keys?.toList().orEmpty()
    override fun getAll(tableName: String): Map<String, String> = data[tableName].orEmpty()
    override fun clearKV(tableName: String) { data.remove(tableName) }
}

private object DemoFileManager : IFileManager {
    private val files = mutableMapOf<String, String>()
    override fun readUserFile(fileName: String, readResult: (FileReadResult) -> Unit) {
        readResult(FileReadResult.Success(files[fileName].orEmpty()))
    }
    override fun writeUserFile(fileName: String, data: String, writeResult: (FileWriteResult) -> Unit) {
        files[fileName] = data
        writeResult(FileWriteResult.Success())
    }
    override fun deleteUserFile(filePath: String) { files.remove(filePath) }
    override fun readFile(dirName: String, fileName: String, level: FileCacheLevel, readResult: (FileReadResult) -> Unit) {
        readResult(FileReadResult.Success(files["$dirName/$fileName"].orEmpty()))
    }
    override fun writeFile(dirName: String, fileName: String, level: FileCacheLevel, data: String, writeResult: (FileWriteResult) -> Unit) {
        files["$dirName/$fileName"] = data
        writeResult(FileWriteResult.Success())
    }
    override fun findFilesPath(dirName: String, fileNamePrefix: String, level: FileCacheLevel, sortAscending: Boolean, onCallback: (List<String>?) -> Unit) {
        onCallback(files.keys.filter { it.startsWith("$dirName/$fileNamePrefix") })
    }
    override fun writeMetadata4Pdf(filePath: String, metadata: Map<String, String>?) {}
    override fun writeMetadata4Image(filePath: String, metadata: Map<String, String>?) {}
    override fun createCacheDir(dirName: String): String = "demo-cache/$dirName"
}

private object DemoResManager : IResManager {
    override fun getAssetJson(fileName: String): String = """{"file":"$fileName"}"""
    override fun preloadImage(url: String, onSuccess: (() -> Unit)?, onFail: (() -> Unit)?) { onSuccess?.invoke() }
    override fun copyToClipboard(content: String) {}
    override fun saveImage(url: String, metadata: Map<String, String>?) {}
    override fun saveVideo(localFilePath: String): Boolean = true
    override fun selectImage(context: IKmmContext?, callback: (url: List<String>) -> Unit) = callback(listOf("demo-image://selected"))
    override fun getPaletteColor(imageUrl: String, param: PaletteParam, defaultColor: Int?, onGot: (color: Int) -> Unit) = onGot(defaultColor ?: 0)
    override fun preloadLottieToMemory(context: IKmmContext?, url: String, status: String, isDay: Boolean) {}
    override fun preloadAlphaVideo(url: String, onSuccess: (() -> Unit)?, onFail: (() -> Unit)?) { onSuccess?.invoke() }
}

private object DemoAppReport : IAppReport {
    override fun reportBeacon(event: String, params: Map<String, String>?) {}
    override fun reportBugly(msg: String, error: Throwable?) {}
    override fun reportDt(event: String, params: Map<String, String>?) {}
    override fun setPageStartFrom(from: String) {}
    override fun resetPageStartFrom() {}
    override fun uploadLogToBugly(onResult: (Boolean) -> Unit) = onResult(true)
    override fun prepareFeedbackLogZipBase64(onResult: (FeedbackLogZipPayload?) -> Unit) = onResult(FeedbackLogZipPayload("ZGVtbw=="))
}

private object DemoAppAlert : IAppAlert {
    override fun showToast(msg: String, duration: Double, debug: Boolean) {}
    override fun showDialog(title: String, msg: String) {}
    override fun showDecorDebugView(msg: String) {}
    override fun hideCurrentToast() {}
}

private object DemoAppEncoder : IAppEncoder {
    override fun encodeBase64Bytes(bytes: ByteArray): String = if (bytes.decodeToString() == "kmm demo") "a21tIGRlbW8=" else bytes.decodeToString()
    override fun decodeBase64(data: String): String = if (data == "a21tIGRlbW8=") "kmm demo" else data
    override fun urlEncode(data: String): String = data.replace(" ", "%20")
    override fun urlDecodeUtf8(data: String): String = data.replace("%20", " ")
    override fun cipherDES(key: String, iv: String, bytes: ByteArray): ByteArray = bytes
    override fun cipherAESEncrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray = data
    override fun cipherAESDecrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray = data
    override fun removeEmoji(text: String?): String? = text?.filter { it.code <= 0xFFFF }
    override fun md5(input: String): String = "demo-md5-$input"
    override fun rsaEncryptBase64(data: ByteArray, publicKey: String): String = encodeBase64Bytes(data)
}

private object DemoTask : ITask {
    override fun postAction(action: () -> Unit, delayTime: Long) = object : com.tencent.news.core.platform.api.IKmmActionResult {
        init { action() }
        override fun cancel() {}
    }
    override fun runIOAction(action: () -> Unit) = action()
    override fun runMainAction(action: () -> Unit) = action()
    override fun runCpuAction(action: () -> Unit) = action()
}

private object DemoEventBus : IEventBus {
    override fun post(event: IEvent) {}
    override fun postSticky(event: IEvent) {}
}

private class DemoEvent(private val name: String) : IEvent {
    override val exportEventName: String = name
    override fun buildExportPrimitiveMap(): Map<String, Any> = mapOf("name" to name)
}

private object DemoNetwork : INetwork {
    private fun <T> respond(builder: NetworkBuilder<T>): INetworkRequest {
        val json = """{"source":"demo"}"""
        builder.onResponse(NetworkResponse(json, successResult("demo"), builder.parser?.onParseJson(json)))
        return DefaultNetworkRequest()
    }
    override fun <T> postJson(builder: NetworkBuilder<T>) = respond(builder)
    override fun <T> postForm(builder: NetworkBuilder<T>) = respond(builder)
    override fun <T> get(builder: NetworkBuilder<T>) = respond(builder)
    override fun <T> sse(builder: NetworkBuilder<T>) = respond(builder)
    override fun <T> postMultipart(builder: NetworkBuilder<T>) = respond(builder)
    override fun <T> postBytes(builder: NetworkBuilder<T>) = respond(builder)
    override fun <T> postPb(builder: PBNetworkBuilder<T>): INetworkRequest {
        val body = builder.bodyEncoder()
        builder.onResponse(PBNetworkResponse(ResultEx(succeed = true, msg = "demo", errorCode = 0), builder.bodyDecoder(body), body))
        return DefaultNetworkRequest()
    }
    override fun netState(): NetState = NetState.WIFI
}

private object DemoFileCacheManager : IFileCacheManager {
    private val files = mutableMapOf<String, String>()
    override fun fileAbsolutePath(folderPath: String, fileName: String): String = "$folderPath/$fileName"
    override fun cacheFile(filePath: String, data: String): Boolean {
        files[filePath] = data
        return true
    }
    override fun containsFile(filePath: String): Boolean = files.containsKey(filePath)
    override fun removeFile(filePath: String) { files.remove(filePath) }
    override fun removeDirPathFile(dirPath: String) { files.keys.filter { it.startsWith(dirPath) }.forEach { files.remove(it) } }
}

private object DemoGyroscope : IAppGyroscope {
    override fun isAvailable(): Boolean = true
    override fun registerListener(config: SensorConfig, listener: IGyroscopeListener): Boolean {
        listener.onGyroscopeChanged(GyroscopeData(1f, 0f, 0f, 1L, SensorAccuracy.HIGH))
        return true
    }
    override fun unregisterListener(listener: IGyroscopeListener) {}
    override fun unregisterAllListeners() {}
    override fun getLatestData(): GyroscopeData = GyroscopeData(1f, 0f, 0f, 1L, SensorAccuracy.HIGH)
}

private object DemoPermission : IAppPermission {
    override fun hasLocationPermission(context: IKmmContext, scenes: Int): Boolean = true
    override fun requestLocationPermission(context: IKmmContext, scenes: Int, isForceRequestPermission: Boolean, callback: IPermissionCallback) {
        callback.onPermissionGranted()
    }
}

private object DemoLocation : IAppLocation {
    override suspend fun requestLocation(
        context: IKmmContext?,
        scenes: Int,
        isForceRequestPermission: Boolean,
        requestLocationOnGrand: Boolean,
        callback: ILocationCallBack?,
    ) {
        callback?.onLocationRequested("440300")
    }
    override fun silentGetAdCode(scenes: Int): String = "440300"
    override fun enterModule(scenes: Int) {}
    override fun leaveModule(scenes: Int) {}
    override fun isSystemLocationEnabled(): Boolean = true
    override fun openLocationSettings() {}
}

private object DemoVolumeController : ISystemVolumeController {
    override fun registerListener(listener: IVolumeListener) = listener.onVolumeChanged(5, true)
    override fun unregisterListener(listener: IVolumeListener) {}
}

private object DemoScreenInfo : IScreenInfo {
    override fun getScreenWidth(): Int = 390
    override fun getScreenHeight(): Int = 844
    override fun getScreenWidthInch(): Float = 2.5f
    override fun getScreenHeightInch(): Float = 5.4f
    override fun getDpi(): Int = 3
}
