package com.tencent.kmm.demo

import com.tencent.kmm.startup.std.config.BuglyInitConfig
import com.tencent.kmm.startup.std.config.BeaconInitConfig
import com.tencent.kmm.startup.std.config.WeComShareInitConfig
import com.tencent.kmm.startup.std.config.WeiboShareInitConfig
import com.tencent.kmm.startup.std.config.QQLoginInitConfig
import com.tencent.kmm.startup.std.config.QimeiInitConfig
import com.tencent.kmm.startup.std.config.ReshubInitConfig
import com.tencent.kmm.startup.std.config.TabExpInitConfig
import com.tencent.kmm.startup.std.config.ToggleInitConfig
import com.tencent.kmm.startup.std.config.TuringInitConfig
import com.tencent.kmm.startup.std.config.WXLoginInitConfig

private val isAlphaBuild = BuildConfig.BUILD_TYPE.equals("alpha", ignoreCase = true)

internal val qimeiConfig = QimeiInitConfig(
    appKey = "0S000EAOIR285SV7",
    appVersion = BuildConfig.VERSION_NAME,
    channelId = BuildConfig.BUILD_TYPE,
    isDebug = BuildConfig.DEBUG,
    userAgreePrivacy = true,
)

internal val turingConfig = TuringInitConfig(
    appId = "2082423467",
    channelId = 105428,
    userId = "",
    userAgreePrivacy = true,
    isDebug = BuildConfig.DEBUG,
)

internal val buglyConfig = BuglyInitConfig(
    appId = "c91a33d0e8",
    appKey = "970e6776-0a85-426e-a97a-742dd4b28556",
    appVersion = BuildConfig.VERSION_NAME,
    buildNumber = BuildConfig.VERSION_CODE.toString(),
    appChannel = BuildConfig.BUILD_TYPE,
    userId = "",
    // alpha 也开启 Bugly 调试日志能力。
    isDebug = BuildConfig.DEBUG || isAlphaBuild,
)

internal val beaconConfig = BeaconInitConfig(
    appKey = "0S000EAOIR285SV7",
    appVersion = BuildConfig.VERSION_NAME,
    channelId = BuildConfig.BUILD_TYPE,
    userId = "",
    userAgreePrivacy = true,
    enableLog = BuildConfig.DEBUG,
)

internal val tabExpConfig = TabExpInitConfig(
    appId = "8801",
    appKey = "97ea3cfb64eeaa1edba65501d0bb3c86",
    sceneId = "",
    appVersion = BuildConfig.VERSION_NAME,
)

internal val qqLoginConfig = QQLoginInitConfig(
    appId = BuildConfig.QQ_APP_ID,
)

internal val wxLoginConfig = WXLoginInitConfig(
    appId = BuildConfig.WX_APP_ID,
)

internal val weiboShareConfig = WeiboShareInitConfig(
    appKey = "1269698370",
)

internal val weComShareConfig = WeComShareInitConfig(
    // Android 端使用 share app id 作为 registerApp 入参。
    shareAppId = "wwauthc8d2d7a989d28694000026",
)

//internal val thumbplayerConfig = ThumbPlayerInitConfig(
//    guid = "qimei36",
//    platformId = 110303,
//    assignedBizId = 20230815,
//    serviceType = 20200208,
//    superPlayerBridge = AndroidSuperPlayerBridge()
//)

internal val toggleConfig = ToggleInitConfig(
    appId = if (isAlphaBuild) {
        "12b8cc38b4"
    } else {
        "b0c0957519"
    },
    appKey = if (isAlphaBuild) {
        "907ac8df-d1b8-4482-981a-4a63bbced497"
    } else {
        "8d278764-9a1a-45e6-bfa7-ca72d2241181"
    },
    appVersion = BuildConfig.VERSION_NAME,
    userId = "",
    deviceId = "qimei36",
    useTestEnv = false,
    // Toggle SDK 默认走正式逻辑环境。
    isDebug = false,
)

internal val reshubConfig = ReshubInitConfig(
    appId = if (isAlphaBuild) {
        "12b8cc38b4"
    } else {
        "b0c0957519"
    },
    appKey = if (isAlphaBuild) {
        "907ac8df-d1b8-4482-981a-4a63bbced497"
    } else {
        "8d278764-9a1a-45e6-bfa7-ca72d2241181"
    },
    appVersion = BuildConfig.VERSION_NAME,
    deviceId = "qimei36",
    useTestEnv = false,
    forceOnlineEnv = BuildConfig.BUILD_TYPE.equals("release", ignoreCase = true),
    isDebug = BuildConfig.DEBUG,
)
