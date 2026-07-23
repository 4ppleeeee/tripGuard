package com.tencent.kmm.demo.setup

import android.content.Intent
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import com.tencent.kmm.demo.BuildConfig
import com.tencent.kmm.demo.KRApplication
import com.tencent.kmm.startup.std.tasks.OaidState
import com.tencent.kmm.startup.std.tasks.QimeiState
import com.tencent.kmm.startup.std.tasks.TuringState
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.push.NotificationAuthorizationStatus
import com.tencent.news.core.push.guide.INotificationGuideConfig

private const val APP_STATUS_PREF = "kmm_demo_app_status"
private const val KEY_APP_LAUNCH_TIMES = "app_launch_times"

internal fun setupAndroidAppStatus() {
    QnPlatformLogic.appStatus = AndroidAppStatusProvider
    AndroidAppStatusProvider.countAppLaunch()
}

internal fun notifyAndroidLaunchIntent(intent: Intent?) {
    AndroidAppStatusProvider.updateLaunchFrom(intent)
}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
private object AndroidAppStatusProvider : IAppStatus {

    private val context: Context
        get() = KRApplication.application

    private val netListeners = mutableSetOf<NetStateChangeListener>()
    private var currentNetState: NetState = NetState.INAVAILABLE
    private var networkCallbackRegistered = false
    private var launchFrom: String = SchemeFrom.ICON

    override fun getVersion(): Int = BuildConfig.VERSION_CODE
    override fun getVersionName(): String = BuildConfig.VERSION_NAME
    override fun getAppName(): String =
        context.applicationInfo.loadLabel(context.packageManager).toString()

    override fun getAppBuildNo(): String = BuildConfig.CI_BUILD_NUM
    override fun getQQAppId(): String = BuildConfig.QQ_APP_ID
    override fun getWxAppId(): String = BuildConfig.WX_APP_ID
    override fun getDtSessionId(): String = ""
    override fun getQIMEI36(): String = QimeiState.qimei36
    override fun getOAID(): String = OaidState.oaid
    override fun getTOAID(): String = TuringState.oaid
    override fun getTAID(): String = TuringState.taidTicket
    override fun getDevId(): String = QimeiState.qimei.ifBlank { QimeiState.qimei36 }
        .ifBlank { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty() }

    override fun isDebug(): Boolean = BuildConfig.DEBUG
    override fun isRdmDebug(): Boolean = BuildConfig.BUILD_TYPE.contains("rdm", ignoreCase = true)
    override fun isGrey(): Boolean = BuildConfig.BUILD_TYPE.contains("beta", ignoreCase = true)
    override fun isIntegrationMode(): Boolean = false
    override fun isTalkbackEnabled(): Boolean {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return manager?.isEnabled == true && manager.isTouchExplorationEnabled
    }
    override fun isBrowseMode(): Boolean = false
    override fun currentTextScaleGradient(): DensityScaleGradient =
        densityScaleGradientFor(getSystemFontScale())
    override fun isNightMode(): Boolean = isSystemNightMode()
    override fun isInReviewMode(): Boolean = false
    override fun isTextMode(): Boolean = false

    override fun setDarkMode(isDark: Boolean) = Unit
    override fun isSystemNightMode(): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    override fun isSupportFollowSystemBackgroundSetting(): Boolean = true
    override fun subscribeTheme(onThemeChanged: (isDark: Boolean) -> Unit) =
        onThemeChanged(isNightMode())

    override fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double =
        scaleRatioFor(gradient).toDouble()
    override fun setScaleRatio(level: DensityScaleGradient) = Unit
    override fun getSystemFontScale(): Float = context.resources.configuration.fontScale
    override fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Double) -> Unit) =
        onTextScaleRatioChanged(getSystemFontScale().toDouble())

    override fun getDefaultFontFamily(): String = "system"
    override fun subscribeFontFamily(onFontFamilyChanged: (String) -> Unit) =
        onFontFamilyChanged(getDefaultFontFamily())

    override fun getBottomBarHeight(): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }
    override fun getNotificationAuthorizationStatus(
        guideConfigIfDenied: INotificationGuideConfig?,
        callback: (status: NotificationAuthorizationStatus) -> Unit
    ) {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        callback(
            if (enabled) {
                NotificationAuthorizationStatus.Authorized
            } else {
                NotificationAuthorizationStatus.NotDetermined
            }
        )
    }

    override fun netState(): NetState {
        currentNetState = readNetState()
        return currentNetState
    }

    override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        netListeners += netStatusListener
        registerNetworkCallbackIfNeeded()
    }

    override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        netListeners -= netStatusListener
        if (netListeners.isEmpty()) {
            unregisterNetworkCallbackIfNeeded()
        }
    }

    override fun getLaunchFrom(): String = launchFrom
    override fun getScreenWidth(): Int = context.resources.displayMetrics.widthPixels
    override fun getScreenHeight(): Int = context.resources.displayMetrics.heightPixels
    override fun getScreenWidthInch(): Float =
        context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.xdpi

    override fun getScreenHeightInch(): Float =
        context.resources.displayMetrics.heightPixels / context.resources.displayMetrics.ydpi

    override fun getDpi(): Int = context.resources.displayMetrics.densityDpi
    override fun getPackageName(): String = context.packageName
    override fun getPackageFirstInstallTime(): Long =
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime }
            .getOrDefault(0L)

    override fun getAppLaunchTimes(): Int =
        context.getSharedPreferences(APP_STATUS_PREF, Context.MODE_PRIVATE)
            .getInt(KEY_APP_LAUNCH_TIMES, 0)

    override fun getHardware(): String = Build.HARDWARE.orEmpty()
    override fun getRomType(): String = Build.MANUFACTURER.orEmpty()
    override fun getStore(): String = BuildConfig.BUILD_TYPE
    override fun getFixedStore(): String = BuildConfig.BUILD_TYPE
    override fun enableSenor(): Boolean = true
    override fun getOsVs(): String = Build.VERSION.RELEASE.orEmpty()
    override fun getTerm(): String = Build.MODEL.orEmpty()

    fun countAppLaunch() {
        val prefs = context.getSharedPreferences(APP_STATUS_PREF, Context.MODE_PRIVATE)
        val nextTimes = prefs.getInt(KEY_APP_LAUNCH_TIMES, 0) + 1
        prefs.edit().putInt(KEY_APP_LAUNCH_TIMES, nextTimes).apply()
    }

    fun updateLaunchFrom(intent: Intent?) {
        launchFrom = when {
            intent == null -> SchemeFrom.ICON
            intent.action == Intent.ACTION_VIEW -> {
                when (intent.data?.scheme?.lowercase()) {
                    "http", "https" -> SchemeFrom.WAP
                    null -> SchemeFrom.OUTSIDE_OPENURL
                    else -> SchemeFrom.APP_LINK
                }
            }
            intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_LAUNCHER) -> SchemeFrom.ICON
            else -> SchemeFrom.UNKNOWN
        }
    }

    private fun readNetState(): NetState {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetState.INAVAILABLE
        val network = manager.activeNetwork ?: return NetState.INAVAILABLE
        val capabilities = manager.getNetworkCapabilities(network) ?: return NetState.INAVAILABLE
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetState.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetState.WWAN
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetState.WWAN
            else -> NetState.INAVAILABLE
        }
    }

    private fun registerNetworkCallbackIfNeeded() {
        if (networkCallbackRegistered) return
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        runCatching {
            manager.registerDefaultNetworkCallback(networkCallback)
            currentNetState = readNetState()
            networkCallbackRegistered = true
        }
    }

    private fun unregisterNetworkCallbackIfNeeded() {
        if (!networkCallbackRegistered) return
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        runCatching { manager.unregisterNetworkCallback(networkCallback) }
        networkCallbackRegistered = false
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            dispatchNetStateIfChanged()
        }

        override fun onLost(network: Network) {
            dispatchNetStateIfChanged()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            dispatchNetStateIfChanged()
        }
    }

    private fun dispatchNetStateIfChanged() {
        val oldState = currentNetState
        val newState = readNetState()
        if (oldState == newState) return
        currentNetState = newState
        netListeners.toList().forEach { it.netStateChanged(oldState, newState) }
    }

    private fun densityScaleGradientFor(fontScale: Float): DensityScaleGradient =
        when {
            fontScale < 0.95F -> DensityScaleGradient.L0
            fontScale < 1.10F -> DensityScaleGradient.L1
            fontScale < 1.25F -> DensityScaleGradient.L2
            fontScale < 1.40F -> DensityScaleGradient.L3
            fontScale < 1.55F -> DensityScaleGradient.L4
            else -> DensityScaleGradient.L5
        }

    private fun scaleRatioFor(gradient: DensityScaleGradient): Float =
        when (gradient) {
            DensityScaleGradient.L0 -> 0.875F
            DensityScaleGradient.L1 -> 1.0F
            DensityScaleGradient.L2 -> 1.125F
            DensityScaleGradient.L3 -> 1.25F
            DensityScaleGradient.L4 -> 1.375F
            DensityScaleGradient.L5 -> 1.5F
        }
}
