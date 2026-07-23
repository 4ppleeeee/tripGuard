package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.IAppWindow
import com.tencent.news.core.platform.api.IAppWindowOrientationSensor
import com.tencent.news.core.platform.api.IDeviceOrientationListener
import com.tencent.news.core.platform.api.ScreenOrientation
import com.tencent.news.core.platform.api.appPageStack
import platform.framework.OH_WindowManager_SetWindowKeepScreenOn

/**
 * 鸿蒙端窗口桥接接口，由 knoi @KNCallback 生成的 ArkTS 侧实现注入。
 * @see com.tencent.news.core.ohos.setup.knoi.callbacks.setupOhosAppWindow
 */
interface OhosAppWindowBridge {
    fun setScreenOrientation(orientation: String)
    fun enterFullScreen()
    fun exitFullScreen()
    fun getScreenOrientation(): String
    fun startDeviceOrientationListening(onOrientationChanged: (orientation: String) -> Unit): Boolean
    fun stopDeviceOrientationListening()
}

/**
 * 鸿蒙平台的 IAppWindow 实现
 *
 * 通过鸿蒙 CAPI OH_NativeWindowManager_SetWindowKeepScreenOn 控制屏幕常亮
 * 屏幕方向通过 [OhosAppWindowBridge] knoi 回调桥接到 ArkTS 侧实现
 * 全屏控制通过 [OhosAppWindowBridge] knoi 回调桥接到 ArkTS 侧实现
 *
 * 参考文档：https://developer.huawei.com/consumer/cn/doc/harmonyos-references/capi-oh-window-h
 */
class OhosAppWindow : IAppWindow, IAppWindowOrientationSensor {

    /**
     * knoi 桥接实例，由 ArkTS 侧通过 setupOhosAppWindow() 注入。
     * 若未注入则相关方法静默失败。
     */
    internal var bridge: OhosAppWindowBridge? = null

    private val deviceOrientationListeners = mutableSetOf<IDeviceOrientationListener>()
    private var isDeviceOrientationListening = false
    private var latestDeviceOrientation: ScreenOrientation? = null

    private fun getWindowId(): Int {
        // 页面栈为空时（如 Application 阶段）返回 0，避免 NPE
        return appPageStack().getTopValidPage()?.getWindowId() ?: 0
    }

    override fun keepScreenOn() {
        OH_WindowManager_SetWindowKeepScreenOn(getWindowId(), true)
    }

    override fun cancelScreenOn() {
        OH_WindowManager_SetWindowKeepScreenOn(getWindowId(), false)
    }

    override fun setScreenOrientation(orientation: ScreenOrientation) {
        val orientationStr = when (orientation) {
            ScreenOrientation.PORTRAIT -> "portrait"
            ScreenOrientation.LANDSCAPE -> "landscape"
            ScreenOrientation.AUTO -> "auto"
        }
        bridge?.setScreenOrientation(orientationStr)
    }

    override fun getScreenOrientation(): ScreenOrientation {
        val orientationStr = bridge?.getScreenOrientation() ?: return ScreenOrientation.PORTRAIT
        return when (orientationStr) {
            "landscape" -> ScreenOrientation.LANDSCAPE
            "auto" -> ScreenOrientation.AUTO
            else -> ScreenOrientation.PORTRAIT
        }
    }

    override fun enterFullScreen() {
        bridge?.enterFullScreen()
    }

    override fun exitFullScreen() {
        bridge?.exitFullScreen()
    }

    override fun registerDeviceOrientationListener(listener: IDeviceOrientationListener): Boolean {
        deviceOrientationListeners.add(listener)
        if (isDeviceOrientationListening) {
            return true
        }
        val started = bridge?.startDeviceOrientationListening { orientation ->
            onNativeDeviceOrientationChanged(orientation)
        } ?: false
        if (!started) {
            deviceOrientationListeners.remove(listener)
            return false
        }
        isDeviceOrientationListening = true
        return true
    }

    override fun unregisterDeviceOrientationListener(listener: IDeviceOrientationListener) {
        if (!deviceOrientationListeners.remove(listener)) return
        if (deviceOrientationListeners.isEmpty()) {
            stopDeviceOrientationListening()
        }
    }

    override fun unregisterAllDeviceOrientationListeners() {
        if (deviceOrientationListeners.isEmpty()) return
        deviceOrientationListeners.clear()
        stopDeviceOrientationListening()
    }

    override fun isAutoRotationEnabled(): Boolean {
        // 鸿蒙暂无公开 API 查询系统旋转锁定状态，默认返回 true
        return true
    }

    private fun stopDeviceOrientationListening() {
        if (!isDeviceOrientationListening) return
        isDeviceOrientationListening = false
        latestDeviceOrientation = null
        bridge?.stopDeviceOrientationListening()
    }

    private fun onNativeDeviceOrientationChanged(orientation: String) {
        val nextOrientation = orientation.toScreenOrientation()
        if (latestDeviceOrientation == nextOrientation) return
        latestDeviceOrientation = nextOrientation
        deviceOrientationListeners.toList().forEach {
            it.onDeviceOrientationChanged(nextOrientation)
        }
    }

    private fun String.toScreenOrientation(): ScreenOrientation {
        return when (this) {
            "landscape" -> ScreenOrientation.LANDSCAPE
            else -> ScreenOrientation.PORTRAIT
        }
    }
}
