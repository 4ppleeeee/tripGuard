package com.tencent.news.core.platform.impl

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.push.NotificationAuthorizationStatus
import com.tencent.news.core.push.guide.INotificationGuideConfig


// 懒加载静态持有下，防止每次调用都创建
// 同时兼顾了宿主 QnPlatformLogic.appStatus 注入的时序问题，即使注入的晚也能切换过来
@KmmInternalApi
internal object AppStatusHolder {

    private var instance: IAppStatus? = null

    private val defaultImpl by lazy { AppStatusInterceptor(DefaultAppStatus()) }

    fun get(): IAppStatus {
        val cached = instance
        if (cached != null) {
            return cached
        }
        val hostImpl = QnPlatformLogic.appStatus
        if (hostImpl != null) {
            val result = AppStatusInterceptor(hostImpl)
            this.instance = result
            return result
        }
        return defaultImpl
    }

}

// 这个类主要用于拦截一些app不会变化的值：
// 尤其针对鸿蒙平台，从target读取数据会跨语言环境，影响性能
private class AppStatusInterceptor(val target: IAppStatus) : IAppStatus by target {

    private var _isDebug: Boolean? = null
    private var _isRdmDebug: Boolean? = null
    private var _isGrey: Boolean? = null

    override fun isDebug(): Boolean {
        val result = _isDebug ?: target.isDebug()
        _isDebug = result
        return result
    }

    override fun isRdmDebug(): Boolean {
        val result = _isRdmDebug ?: target.isRdmDebug()
        _isRdmDebug = result
        return result
    }

    override fun isGrey(): Boolean {
        val result = _isGrey ?: target.isGrey()
        _isGrey = result
        return result
    }

}

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
private class DefaultAppStatus : IAppStatus {

    // IExportAppStatus
    override fun getVersion(): Int = 0
    override fun getVersionName(): String = ""
    override fun getAppName(): String = ""
    override fun getAppBuildNo(): String = ""
    override fun getQQAppId(): String = ""
    override fun getWxAppId(): String = ""
    override fun getDtSessionId() = ""
    override fun getQIMEI36() = "fcaa0d9bcd98f0edb484464910001aa18810"
    override fun getOAID() = ""
    override fun getTOAID() = ""
    override fun getTAID() = ""
    override fun getDevId(): String = ""
    override fun isDebug(): Boolean = true
    override fun isRdmDebug(): Boolean = false
    override fun isGrey(): Boolean = false
    override fun isIntegrationMode(): Boolean = false
    override fun isTalkbackEnabled(): Boolean = false
    override fun isBrowseMode(): Boolean = false
    override fun currentTextScaleGradient(): DensityScaleGradient = DensityScaleGradient.L1
    override fun isNightMode(): Boolean = false
    override fun isInReviewMode(): Boolean = false
    override fun isTextMode(): Boolean = false

    // IAppStatus
    override fun setDarkMode(isDark: Boolean) {}
    override fun isSystemNightMode(): Boolean = false
    override fun isSupportFollowSystemBackgroundSetting(): Boolean = true
    override fun subscribeTheme(onThemeChanged: (isDark: Boolean) -> Unit) {}
    override fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double = 1.0
    override fun setScaleRatio(level: DensityScaleGradient) {}
    override fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Double) -> Unit) {}
    override fun getDefaultFontFamily(): String = ""
    override fun subscribeFontFamily(onFontFamilyChanged: (String) -> Unit) {}
    override fun getBottomBarHeight(): Int = 0
    override fun getNotificationAuthorizationStatus(
        guideConfigIfDenied: INotificationGuideConfig?,
        callback: (status: NotificationAuthorizationStatus) -> Unit
    ) {}
    override fun netState(): NetState = NetState.WIFI
    override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {}
    override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {}
    override fun getLaunchFrom(): String = SchemeFrom.ICON
    @KmmInternalApi
    override fun getScreenWidth(): Int = 0
    @KmmInternalApi
    override fun getScreenHeight(): Int = 0
    override fun getScreenWidthInch(): Float = 0F
    override fun getScreenHeightInch(): Float = 0F
    override fun getDpi(): Int = 0
    override fun getPackageName(): String = ""
    override fun getPackageFirstInstallTime(): Long = 0L
    override fun getAppLaunchTimes(): Int = 0
    override fun getHardware(): String = ""
    override fun getRomType(): String = ""
    override fun getStore(): String = ""
    override fun getFixedStore(): String = ""
    override fun enableSenor(): Boolean = false
    override fun getOsVs(): String = ""
    override fun getTerm(): String = ""
}
