package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.AppId
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.knoi.cacheIfNotEmpty
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.ohos.utils.OhosPlatformUtil
import com.tencent.news.core.ohos.utils.OhosScreenUtils
import com.tencent.news.core.ohos.setup.knoi.consumer.ohosNetworkService
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.platform.api.export.IExportAppStatus
import com.tencent.news.core.push.NotificationAuthorizationStatus
import com.tencent.news.core.push.guide.INotificationGuideConfig
import com.tencent.news.core.service.AppService
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppStatus = JSValue

/**
 * 企业分发 RDM 包的 bundleId。
 *
 * 用于区分微信/其它三方 AppID 的分包策略。
 */
private const val BUNDLE_ID_RDM_SUFFIX = ".rdm"
private const val READ_ONLY_MODE_TABLE = "sp_read_only_mode"
private const val KEY_READ_ONLY_STATUS = "read_only_mode_status"
private const val KEY_PRIVACY_AGREED_VERSION = "privacy_agreed_version"
private const val READ_ONLY_STATUS_ENABLED = "1"
private const val PRIVACY_UNHANDLED_VERSION = "0"

fun setupOhosAppStatus(status: IOhosAppStatus, isDebug: Boolean = false) {
    OhosPlatformUtil.isDebug = isDebug
    QnPlatformLogic.appStatus = OhosAppStatusProvider(status.asOhosAppStatus())
}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
internal class OhosAppStatusProvider(
    private val ohosStatus: OhosAppStatus
) : IAppStatus, IExportAppStatus by AppService.status {

    private val netListeners = mutableListOf<NetStateChangeListener>()
    private var currentNetState: NetState = NetState.WIFI
    private var netStateSubscribed = false

    // 【AppId】
    override fun getQQAppId(): String = AppId.QQ_LOGIN

    /**
     * 微信开放平台 AppID。
     *
     * RDM/企业分发包使用独立 AppID，普通包使用 release AppID。
     */
    override fun getWxAppId(): String =
        if (getPackageName().endsWith(BUNDLE_ID_RDM_SUFFIX)) AppId.WX_RDM_APP_ID else AppId.WX_RELEASE_APP_ID

    // 【App信息】
    override fun getAppName(): String = OhosPlatformUtil.getAppName()
    override fun getPackageName(): String = OhosPlatformUtil.getPackageName()

    override fun getStore(): String = "96"
    override fun getFixedStore(): String = "12981"

    // 【设备信息】
    @OptIn(KmmInternalApi::class)
    override fun getScreenWidth(): Int = OhosScreenUtils.getScreenWidth()

    @OptIn(KmmInternalApi::class)
    override fun getScreenHeight(): Int = OhosScreenUtils.getScreenHeight()
    override fun getScreenWidthInch(): Float = OhosScreenUtils.getDeviceWidthDp()
    override fun getScreenHeightInch(): Float = OhosScreenUtils.getDeviceHeightDp()
    override fun getDpi(): Int = OhosScreenUtils.getScreenDpi()
    override fun getHardware(): String = cacheIfNotEmpty { OhosPlatformUtil.getHardware() }
    override fun getRomType(): String = cacheIfNotEmpty { OhosPlatformUtil.getRomType() }

    // 【App状态】
    override fun isDebug(): Boolean = OhosPlatformUtil.isDebug
    override fun isRdmDebug(): Boolean = OhosPlatformUtil.isRdm() || OhosPlatformUtil.isDebug
    override fun isGrey() = false
    override fun isTalkbackEnabled(): Boolean = false
    override fun isIntegrationMode(): Boolean = false
    override fun isBrowseMode(): Boolean {
        return try {
            val storage = com.tencent.news.core.platform.api.appStorage()
            val isReadOnly = storage.getKV(
                READ_ONLY_MODE_TABLE,
                KEY_READ_ONLY_STATUS,
                "0"
            ) == READ_ONLY_STATUS_ENABLED
            val isPrivacyUnhandled = storage.getKV(
                READ_ONLY_MODE_TABLE,
                KEY_PRIVACY_AGREED_VERSION,
                PRIVACY_UNHANDLED_VERSION
            ) == PRIVACY_UNHANDLED_VERSION
            isReadOnly || isPrivacyUnhandled
        } catch (_: Exception) {
            false
        }
    }
    override fun isNightMode(): Boolean = AppService.status.isNightMode()
    override fun getDtSessionId(): String = "1"

    override fun subscribeTheme(onThemeChanged: (Boolean) -> Unit) {
        ohosStatus.subscribeTheme { jsValueArray ->
            val isDarkMode = jsValueArray.firstOrNull()?.toBoolean() ?: isNightMode()
            onThemeChanged.invoke(isDarkMode)
        }
    }

    override fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Double) -> Unit) {
        ohosStatus.subscribeTextScaleRatio { jsValueArray ->
            val scale = jsValueArray.firstOrNull()?.toDouble() ?: 1.0
            OhosDensityManager.onTextScaleChanged(scale)
            onTextScaleRatioChanged.invoke(scale)
        }
    }

    override fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double {
        return OhosDensityManager.getScaleRatioByGradient(gradient)
    }

    override fun setScaleRatio(level: DensityScaleGradient) {
        AppService.status.setTextScaleLevel(level.level)
    }

    override fun getDefaultFontFamily(): String = ""

    override fun subscribeFontFamily(onFontFamilyChanged: (String) -> Unit) {
        ohosStatus.subscribeFontFamily { jsValueArray ->
            val fontName = jsValueArray.firstOrNull()?.toKString() ?: getDefaultFontFamily()
            onFontFamilyChanged.invoke(fontName)
        }
    }

    override fun setDarkMode(isDark: Boolean) {}
    override fun isSystemNightMode(): Boolean = false
    override fun isSupportFollowSystemBackgroundSetting(): Boolean = true
    override fun getBottomBarHeight(): Int = 0
    override fun getNotificationAuthorizationStatus(
        guideConfigIfDenied: INotificationGuideConfig?,
        callback: (status: NotificationAuthorizationStatus) -> Unit
    ) {}
    override fun getLaunchFrom(): String = SchemeFrom.ICON
    override fun enableSenor(): Boolean = false
    override fun getOsVs(): String = ""
    override fun getTerm(): String = ""

    override fun netState(): NetState = currentNetState

    override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        netListeners.add(netStatusListener)
        ensureNetStateSubscribed()
    }

    override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        netListeners.remove(netStatusListener)
    }

    private fun ensureNetStateSubscribed() {
        if (netStateSubscribed) return
        netStateSubscribed = true
        runCatching {
            ohosStatus.subscribeNetState { jsValueArray ->
                val stateName = jsValueArray.firstOrNull()?.toKString() ?: return@subscribeNetState
                val newState = NetState.values().firstOrNull { it.nameStr == stateName }
                    ?: NetState.WIFI
                val oldState = currentNetState
                if (oldState != newState) {
                    currentNetState = newState
                    netListeners.toList().forEach { listener: NetStateChangeListener ->
                        runCatching {
                            listener.netStateChanged(oldState, newState)
                        }
                    }
                }
            }
        }
    }
}

@KNCallback
interface OhosAppStatus {

    fun subscribeTheme(onUiModeChanged: (Array<JSValue?>) -> Unit)

    fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Array<JSValue?>) -> Unit)

    fun subscribeFontFamily(onFontFamilyChanged: (Array<JSValue?>) -> Unit)

    fun subscribeNetState(onChange: (Array<JSValue?>) -> Unit)
}
