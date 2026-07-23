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
    fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Double) -> Unit) {}   // 订阅文字缩放比例变化
    fun getDefaultFontFamily(): String = ""
    fun subscribeFontFamily(onFontFamilyChanged: (String) -> Unit) {}
    // 查询字体资源是否已完成下载/注册。真实查询由宿主 IAppStatus 实现；默认返回 false，避免误认为 QnCore 有资源检测能力。
    fun isFontResourceReady(fontId: String, fontFamily: String): Boolean = false
    // 显式触发字体下载流程；宿主应在方法入口感知下载请求，并通过 callback 回传下载状态。
    fun downloadFontResource(
        fontId: String,
        fontFamily: String,
        callback: (status: AppFontDownloadStatus, progress: Double) -> Unit
    ) {
        // QnCore 不直接持有 Shiply/ResHub 字体资源能力；没有宿主 override 时明确失败，避免误认为走了真实下载。
        callback(AppFontDownloadStatus.FAILED, 0.0)
    }
    fun getBottomBarHeight(): Int = 0

    fun canAutoPlayListVideo(channel: String) = false

    fun getNotificationAuthorizationStatus(
        guideConfigIfDenied: INotificationGuideConfig?,     // 如果guideConfigIfDenied为空，则拒绝后不弹出跳转系统设置通知页面的引导
        callback: (status: NotificationAuthorizationStatus) -> Unit
    ) {
    }

    fun netState(): NetState = NetState.INAVAILABLE                                    // 获取当前网络状态
    fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener)
    fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener)

    fun getLaunchFrom(): String = SchemeFrom.ICON

    // 设备信息
    fun getScreenWidth(): Int = 0                   // 屏幕宽度px

    fun getScreenHeight(): Int = 0                  // 屏幕高度px
    fun getScreenWidthInch(): Float = 0F
    fun getScreenHeightInch(): Float = 0F
    fun getDpi(): Int = 0

    fun getPackageName(): String = ""
    fun getHardware(): String = ""                  // 硬件信息
    fun getRomType(): String = ""                   // rom类型

    fun getStore(): String = ""                     // 渠道号
    fun getFixedStore(): String = ""                // 固定渠道号

    fun enableSenor(): Boolean = false
    fun getOsVs(): String = ""                      // 系统版本号
    fun getTerm(): String = ""                      // 硬件型号

    // 兼容旧 QnCore 的无 import 调用；与 AppStatusEx 中的转换规则保持一致。
    fun getAppVersionName(): String {
        val appVersion = getVersion().toString()
        return if (appVersion.length == 4) {
            "${appVersion[0]}.${appVersion[1]}.${appVersion.substring(2)}"
        } else {
            appVersion
        }
    }

    fun isLoginJumpBack(): Boolean = false          // 上次退后台是否是登陆拉起的，为了区别切换前后台和拉起登陆情况，判断比较宽松，不严格
}

interface NetStateChangeListener {
    fun netStateChanged(old: NetState, new: NetState)
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
