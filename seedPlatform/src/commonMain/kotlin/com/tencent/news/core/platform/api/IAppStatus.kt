package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.platform.api.export.IExportAppStatus
import com.tencent.news.core.platform.impl.AppStatusHolder
import com.tencent.news.core.push.NotificationAuthorizationStatus
import com.tencent.news.core.push.guide.INotificationGuideConfig

enum class AppFontDownloadStatus {
    DOWNLOADING,
    COMPLETED,
    FAILED
}

@Suppress("MaxLineLength")
interface IAppStatus : IExportAppStatus {

    // 【应用内各种模式切换】：
    fun setDarkMode(isDark: Boolean) {}     // 切换到夜间模式
    fun isSystemNightMode(): Boolean = isNightMode()      // 读取系统当前真实的日夜间状态
    fun isSupportFollowSystemBackgroundSetting(): Boolean = false
    fun subscribeTheme(onThemeChanged: (isDark: Boolean) -> Unit) {}
    fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double = 1.0         // 根据梯度获取缩放比例
    fun setScaleRatio(level: DensityScaleGradient) {}     // 设置文字缩放比例
    fun getSystemFontScale(): Float = 1.0F      // 系统字体缩放比例
    fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Double) -> Unit) {}   // 订阅文字缩放比例变化
    fun getDefaultFontFamily(): String = ""
    fun subscribeFontFamily(onFontFamilyChanged: (String) -> Unit) {}
    fun isFontResourceReady(fontId: String, fontFamily: String): Boolean = false
    fun downloadFontResource(
        fontId: String,
        fontFamily: String,
        callback: (status: AppFontDownloadStatus, progress: Double) -> Unit
    ) {
        callback(AppFontDownloadStatus.FAILED, 0.0)
    }
    fun getBottomBarHeight(): Int = 0

    fun canAutoPlayListVideo(channel: String) = false

    fun getNotificationAuthorizationStatus(
        guideConfigIfDenied: INotificationGuideConfig?,     // 如果guideConfigIfDenied为空，则拒绝后不弹出跳转系统设置通知页面的引导
        callback: (status: NotificationAuthorizationStatus) -> Unit
    ) {
    }

    @Deprecated("Use appNetwork().netState() instead", ReplaceWith("appNetwork().netState()"))
    fun netState(): NetState = NetState.INAVAILABLE                                    // 获取当前网络状态

    @Deprecated(
        "Use appNetwork().addNetStatusChangeListener(netStatusListener) instead",
        ReplaceWith("appNetwork().addNetStatusChangeListener(netStatusListener)")
    )
    fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {}

    @Deprecated(
        "Use appNetwork().removeNetStatusChangeListener(netStatusListener) instead",
        ReplaceWith("appNetwork().removeNetStatusChangeListener(netStatusListener)")
    )
    fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {}

    fun getLaunchFrom(): String = SchemeFrom.ICON

    // 设备信息
    @Deprecated("Use appScreenInfo().getScreenWidth() instead", ReplaceWith("appScreenInfo().getScreenWidth()"))
    fun getScreenWidth(): Int = 0                   // 屏幕宽度px

    @Deprecated("Use appScreenInfo().getScreenHeight() instead", ReplaceWith("appScreenInfo().getScreenHeight()"))
    fun getScreenHeight(): Int = 0                  // 屏幕高度px

    @Deprecated(
        "Use appScreenInfo().getScreenWidthInch() instead",
        ReplaceWith("appScreenInfo().getScreenWidthInch()")
    )
    fun getScreenWidthInch(): Float = 0F

    @Deprecated(
        "Use appScreenInfo().getScreenHeightInch() instead",
        ReplaceWith("appScreenInfo().getScreenHeightInch()")
    )
    fun getScreenHeightInch(): Float = 0F

    @Deprecated("Use appScreenInfo().getDpi() instead", ReplaceWith("appScreenInfo().getDpi()"))
    fun getDpi(): Int = 0

    fun getPackageName(): String = ""
    fun getPackageFirstInstallTime(): Long = 0L
    fun getAppLaunchTimes(): Int = 0
    fun getHardware(): String = ""                  // 硬件信息
    fun getRomType(): String = ""                   // rom类型

    fun getStore(): String = ""                     // 渠道号
    fun getFixedStore(): String = ""                // 固定渠道号

    fun enableSenor(): Boolean = false
    fun getOsVs(): String = ""                      // 系统版本号
    fun getTerm(): String = ""                      // 硬件型号

}

// 按照习惯，iOS的debug代表本地开发，rdm包是通过rdm判断，但是安卓的rdm会当作debug，所以后续考虑废弃容易出现歧义的名称，改为 isRdm 和 isLocalDebug
fun isDebug(): Boolean = appStatus().isDebug()

fun isLocalDebug(): Boolean = appStatus().isDebug() && !appStatus().isRdmDebug()

fun runDebug(action: () -> Unit) {
    if (isDebug()) {
        action()
    }
}

fun isTalkbackEnabled(): Boolean = appStatus().isTalkbackEnabled()

fun isGrey(): Boolean = appStatus().isGrey()

fun isRdm(): Boolean = appStatus().isRdmDebug()

@OptIn(KmmInternalApi::class)
fun appStatus(): IAppStatus = AppStatusHolder.get()

// 仅浏览模式（这个是比较宽泛的判定，用户可能处于未点击授权弹窗的状态，这个也是true）
fun isAppBrowserMode(): Boolean = appStatus().isBrowseMode()

// 先简单实现下，后续再改
fun isDemo(): Boolean = appStatus().getVersion() == 0
