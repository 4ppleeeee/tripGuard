package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.AppId
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.knoi.cacheIfNotEmpty
import com.tencent.news.core.list.trace.NewsAudioLog
import com.tencent.news.core.ohos.utils.OhosPlatformUtil
import com.tencent.news.core.ohos.utils.OhosScreenUtils
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.AppFontDownloadStatus
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.platform.api.export.IExportAppStatus
import com.tencent.news.core.platform.synchronized
import com.tencent.news.core.service.AppService
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias IOhosAppStatus = JSValue

fun setupOhosAppStatus(status: IOhosAppStatus) {
    QnPlatformLogic.appStatus = OhosAppStatusProvider(status.asOhosAppStatus())
}

internal class OhosAppStatusProvider(
    private val ohosStatus: OhosAppStatus
) : IAppStatus, IExportAppStatus by AppService.status {
    // 网络状态监听器缓存列表
    private val netStateListeners = mutableListOf<NetStateChangeListener>()
    private val netStateListenersLock = Lock()

    // 【AppId】
    override fun getQQAppId(): String = AppId.QQ_LOGIN
    override fun getWxAppId(): String =
        if (isRdmDebug()) AppId.WX_DEBUG_APP_ID else AppId.WX_RELEASE_APP_ID

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
    override fun isRdmDebug(): Boolean = OhosPlatformUtil.isRdm()
    override fun isGrey() = false
    override fun isTalkbackEnabled(): Boolean = false
    override fun isIntegrationMode(): Boolean = false
    override fun isBrowseMode(): Boolean = false
    override fun isNightMode(): Boolean = AppService.status.isNightMode()
    override fun isPersonalizedSwitchOpen(): Boolean = AppService.status.isPersonalizedSwitchOpen()
    override fun isInNewsTopManualMode(): Boolean = AppService.status.isInNewsTopManualMode()
    override fun getDtSessionId(): String = "1"
    override fun enableSenor(): Boolean = QnPlatformLogic.gyroscope?.isAvailable() ?: false
    override fun isLoginJumpBack(): Boolean = false

    override fun subscribeTheme(onThemeChanged: (Boolean) -> Unit) {
        ohosStatus.subscribeTheme { jsValueArray ->
            val isDarkMode = jsValueArray.firstOrNull()?.toBoolean() ?: isNightMode()
            onThemeChanged.invoke(isDarkMode)
        }
    }

    override fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Double) -> Unit) {
        ohosStatus.subscribeTextScaleRatio { jsValueArray ->
            val scale = jsValueArray.getOrNull(0)?.toDouble() ?: 1.0
            val level = jsValueArray.getOrNull(1)?.toDouble()?.toInt()
            OhosDensityManager.onTextScaleChanged(scale, level)
            onTextScaleRatioChanged.invoke(scale)
        }
    }

    override fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double {
        return OhosDensityManager.getScaleRatioByGradient(gradient)
    }

    override fun getDefaultFontFamily(): String = ohosStatus.getDefaultFontFamily()

    override fun subscribeFontFamily(onFontFamilyChanged: (String) -> Unit) {
        ohosStatus.subscribeFontFamily { jsValueArray ->
            val fontName = jsValueArray.firstOrNull()?.toKString() ?: getDefaultFontFamily()
            onFontFamilyChanged.invoke(fontName)
        }
    }

    override fun isFontResourceReady(fontId: String, fontFamily: String): Boolean {
        return ohosStatus.isFontResourceReady(fontId, fontFamily)
    }

    override fun downloadFontResource(
        fontId: String,
        fontFamily: String,
        callback: (status: AppFontDownloadStatus, progress: Double) -> Unit
    ) {
        ohosStatus.downloadFontResource(fontId, fontFamily) { jsValueArray ->
            val status = jsValueArray.getOrNull(0)?.toKString().toAppFontDownloadStatus()
            val progress = jsValueArray.getOrNull(1)?.toDouble() ?: status.defaultProgress()
            callback.invoke(status, progress)
        }
    }


    override fun canAutoPlayListVideo(channel: String): Boolean = false

    override fun addNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        synchronized(netStateListenersLock) {
            if (netStateListeners.isEmpty()) {
                // 第一个监听器加入时，切到主线程真正向 ohos 侧注册
                CoroutineScope(Dispatchers.Main).launch {
                    ohosStatus.subscribeNetState { jsValueArray ->
                        val oldState =
                            jsValueArray.getOrNull(0)?.toKString()?.toNetState()
                                ?: NetState.INAVAILABLE
                        val newState =
                            jsValueArray.getOrNull(1)?.toKString()?.toNetState()
                                ?: NetState.INAVAILABLE
                        // 拷贝快照再遍历，避免遍历时并发修改
                        val listeners = synchronized(netStateListenersLock) {
                            netStateListeners.toList()
                        }
                        NewsAudioLog.debug("OhosAppStatus") {
                            "subscribeNetState callback from ohos, oldState=$oldState, newState=$newState, listenerCount=${listeners.size}"
                        }
                        listeners.forEach { listener ->
                            listener.netStateChanged(oldState, newState)
                        }
                    }
                }
            }
            netStateListeners.add(netStatusListener)
        }
    }

    override fun removeNetStatusChangeListener(netStatusListener: NetStateChangeListener) {
        synchronized(netStateListenersLock) {
            netStateListeners.remove(netStatusListener)
            if (netStateListeners.isEmpty()) {
                NewsAudioLog.debug("OhosAppStatus") {
                    "addNetStatusChangeListener is empty"
                }
                // 最后一个监听器移除时，切到主线程真正向 ohos 侧取消注册
                CoroutineScope(Dispatchers.Main).launch {
                    ohosStatus.unsubscribeNetState()
                }
            }
        }
    }

    override fun setDarkMode(isDarkMode: Boolean) {
        ohosStatus.setDarkMode(isDarkMode)
    }
}

private fun String.toNetState(): NetState {
    return when (this) {
        "wifi" -> NetState.WIFI
        "wwan" -> NetState.WWAN
        else -> NetState.INAVAILABLE
    }
}

@KNCallback
interface OhosAppStatus {

    fun subscribeTheme(onUiModeChanged: (Array<JSValue?>) -> Unit)

    fun subscribeTextScaleRatio(onTextScaleRatioChanged: (Array<JSValue?>) -> Unit)

    fun getDefaultFontFamily(): String

    fun subscribeFontFamily(onFontFamilyChanged: (Array<JSValue?>) -> Unit)

    fun isFontResourceReady(fontId: String, fontFamily: String): Boolean

    fun downloadFontResource(
        fontId: String,
        fontFamily: String,
        onFontDownloadStatusChanged: (Array<JSValue?>) -> Unit
    )

    fun subscribeNetState(onNetStateChanged: (Array<JSValue?>) -> Unit)

    fun unsubscribeNetState()

    fun setDarkMode(isDarkMode: Boolean)

}

private fun String?.toAppFontDownloadStatus(): AppFontDownloadStatus {
    return when (this) {
        AppFontDownloadStatus.DOWNLOADING.name -> AppFontDownloadStatus.DOWNLOADING
        AppFontDownloadStatus.COMPLETED.name -> AppFontDownloadStatus.COMPLETED
        else -> AppFontDownloadStatus.FAILED
    }
}

private fun AppFontDownloadStatus.defaultProgress(): Double {
    return when (this) {
        AppFontDownloadStatus.COMPLETED -> 1.0
        else -> 0.0
    }
}
