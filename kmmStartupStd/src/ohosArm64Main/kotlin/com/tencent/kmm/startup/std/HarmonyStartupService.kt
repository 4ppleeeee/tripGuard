package com.tencent.kmm.startup.std

import com.tencent.tmm.knoi.annotation.ServiceConsumer

internal val hmyStartupService: HarmonyStartupService = getHarmonyStartupServiceApi()

@ServiceConsumer
interface HarmonyStartupService {

    fun initQimei(
        appKey: String,
        channelId: String,
        isDebug: Boolean,
        callback: (qimei36: String, qimei16: String) -> Unit
    )

    fun getUskey(
        appKey: String,
        appVersion: String,
        businessId: String,
        qimei36: String,
        busInfo: String
    ): String

    fun initBugly(
        appId: String,
        appKey: String,
        qimeiAppKey: String,
        qimeiChannelId: String,
        appVersion: String,
        buildNumber: String,
        userId: String,
        appChannel: String,
        isDebug: Boolean,
        callback: () -> Unit
    )

    fun initBeacon(
        appKey: String,
        appVersion: String,
        packageName: String,
        qimeiAppKey: String,
        qimeiChannelId: String,
        isDebug: Boolean,
        userAgreePrivacy: Boolean,
        callback: () -> Unit
    )

    fun initReshub(
        appId: String,
        appKey: String,
        appVersion: String,
        qimei: String,
        useTestEnv: Boolean,
        isDebug: Boolean,
        callback: (env: String) -> Unit
    )

    fun initToggle(
        appId: String,
        appKey: String,
        appVersion: String,
        userId: String,
        deviceId: String,
        useTestEnv: Boolean,
        isDebug: Boolean,
        callback: (env: String) -> Unit
    )

    fun switchToggleUser(userId: String)

    fun initUploadSdk(
        bizAppId: Int,
        bizDomain: String,
        callback: () -> Unit
    )

    fun initTuring(
        appId: String,
        channelId: Int,
        userId: String,
        isDebug: Boolean,
        callback: (
            openIdTicket: String,
            aidTicket: String,
            taidTicket: String,
            oaid: String
        ) -> Unit
    )

    fun initQQLogin(
        appId: String,
        callback: () -> Unit
    )

    fun initWXLogin(
        appId: String,
        callback: () -> Unit
    )

    fun initMmkv(
        callback: () -> Unit
    )

    fun initTabExp(
        appId: String,
        appVersion: String,
        isDebug: Boolean,
        callback: () -> Unit
    )
}
