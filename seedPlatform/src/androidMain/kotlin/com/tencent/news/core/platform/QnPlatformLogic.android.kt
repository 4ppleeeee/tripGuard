package com.tencent.news.core.platform

import com.tencent.news.core.audio.api.IFileCacheManager
import com.tencent.news.core.platform.api.IAppAlert
import com.tencent.news.core.platform.api.IAppConfig
import com.tencent.news.core.platform.api.IAppDevice
import com.tencent.news.core.platform.api.IAppEncoder
import com.tencent.news.core.platform.api.IAppGyroscope
import com.tencent.news.core.platform.api.IAppInstallInfo
import com.tencent.news.core.platform.api.IAppLocation
import com.tencent.news.core.platform.api.IAppMediaPicker
import com.tencent.news.core.platform.api.IAppPageStack
import com.tencent.news.core.platform.api.IAppPermission
import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IAppReport
import com.tencent.news.core.platform.api.IAppSecretConfig
import com.tencent.news.core.platform.api.IAppSkinManager
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.IAppUri
import com.tencent.news.core.platform.api.IAppVibration
import com.tencent.news.core.platform.api.IEventBus
import com.tencent.news.core.platform.api.IFileManager
import com.tencent.news.core.platform.api.INetwork
import com.tencent.news.core.platform.api.IResManager
import com.tencent.news.core.platform.api.IScreenInfo
import com.tencent.news.core.platform.api.IStatusBarController
import com.tencent.news.core.platform.api.IStorage
import com.tencent.news.core.platform.api.ITask
import com.tencent.news.core.platform.api.IAppWindow

internal actual fun getDefaultPlatformLogic(): IPlatformLogic? = AndroidPlatformLogic

private object AndroidPlatformLogic : IPlatformLogic {
    override val appDevice: IAppDevice?
        get() = null
    override val appStatus: IAppStatus?
        get() = null
    override val appConfig: IAppConfig?
        get() = null
    override val appSecretConfig: IAppSecretConfig?
        get() = null
    override val appInstallInfo: IAppInstallInfo?
        get() = null
    override val appStorage: IStorage?
        get() = null
    override val fileManager: IFileManager?
        get() = null
    override val resManager: IResManager?
        get() = null
    override val skinManager: IAppSkinManager?
        get() = null
    override val appReport: IAppReport?
        get() = null
    override val appAlert: IAppAlert
        get() = AndroidAppAlert()
    override val appEncoder: IAppEncoder?
        get() = null
    override val appUri: IAppUri?
        get() = null
    override val task: ITask?
        get() = null
    override val eventBus: IEventBus?
        get() = null
    override val network: INetwork?
        get() = null
    override val appPageStack: IAppPageStack = AndroidAppPageStack()
    override val fileCacheManager: IFileCacheManager?
        get() = null
    override val vibration: IAppVibration
        get() = AndroidAppVibration()
    override val gyroscope: IAppGyroscope?
        get() = null
    override val appPermission: IAppPermission?
        get() = null
    override val appLocation: IAppLocation?
        get() = null
    override val appRegex: IAppRegex?
        get() = null
    override val systemVolumeController: ISystemVolumeController?
        get() = null
    override val statusBarController: IStatusBarController?
        get() = null
    override val appWindow: IAppWindow
        get() = AndroidAppWindow()
    override val screenInfo: IScreenInfo?
        get() = null
    override val appMediaPicker: IAppMediaPicker?
        get() = null
}
