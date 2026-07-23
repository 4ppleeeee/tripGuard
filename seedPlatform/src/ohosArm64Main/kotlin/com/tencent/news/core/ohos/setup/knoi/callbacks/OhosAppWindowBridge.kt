package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.platform.OhosAppWindow
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.qnLogcat
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppWindowBridge = JSValue

/**
 * 注入鸿蒙端 [OhosAppWindowBridge] 实现。
 *
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧基于 @kit.ArkUI 的
 * window.setPreferredOrientation 真实实现桥接到 KMP 层。
 *
 * ArkTS 侧通过 Kommon.setup 调用 getHarmonyStartupProvider().setupAppWindow(new
 * OhosAppWindowBridgeCallback()) 注入实现。
 */
fun setupOhosAppWindow(bridge: IOhosAppWindowBridge) {
    val ohosWindow = QnPlatformLogic.appWindow as? OhosAppWindow ?: return
    ohosWindow.bridge = OhosAppWindowBridgeProvider(bridge.asOhosAppWindowBridge())
}

/**
 * Kotlin 侧的桥接实现：所有方法转发给 ArkTS 侧实现。
 */
private class OhosAppWindowBridgeProvider(
    private val native: OhosAppWindowBridge,
) : com.tencent.news.core.platform.OhosAppWindowBridge {

    private companion object {
        const val TAG = "OhosAppWindow"
    }

    override fun setScreenOrientation(orientation: String) {
        runCatching { native.setScreenOrientation(orientation) }
            .onFailure { qnLogcat()?.logE(TAG, "setScreenOrientation failed", it) }
    }

    override fun enterFullScreen() {
        runCatching { native.enterFullScreen() }
            .onFailure { qnLogcat()?.logE(TAG, "enterFullScreen failed", it) }
    }

    override fun exitFullScreen() {
        runCatching { native.exitFullScreen() }
            .onFailure { qnLogcat()?.logE(TAG, "exitFullScreen failed", it) }
    }

    override fun getScreenOrientation(): String {
        return runCatching { native.getScreenOrientation() }
            .onFailure { qnLogcat()?.logE(TAG, "getScreenOrientation failed", it) }
            .getOrDefault("portrait")
    }

    override fun startDeviceOrientationListening(onOrientationChanged: (orientation: String) -> Unit): Boolean {
        return runCatching { native.startDeviceOrientationListening(onOrientationChanged) }
            .onFailure { qnLogcat()?.logE(TAG, "startDeviceOrientationListening failed", it) }
            .getOrDefault(false)
    }

    override fun stopDeviceOrientationListening() {
        runCatching { native.stopDeviceOrientationListening() }
            .onFailure { qnLogcat()?.logE(TAG, "stopDeviceOrientationListening failed", it) }
    }
}

/**
 * ArkTS 侧窗口管理能力接口。
 *
 * knoi 编译时会自动生成 ArkTS 侧的接口定义，ArkTS 侧 OhosAppWindowBridgeCallback
 * 实现该接口并通过 getHarmonyStartupProvider().setupAppWindow 注入。
 *
 * ArkTS 侧实现要点：
 * - setScreenOrientation: 使用 window.setPreferredOrientation()
 *   - "portrait" → window.Orientation.PORTRAIT
 *   - "landscape" → window.Orientation.LANDSCAPE
 *   - "auto" → window.Orientation.UNSPECIFIED
 * - startDeviceOrientationListening: 使用加速度计监听手机物理姿态变化；
 *   Kotlin 侧只接收 "portrait" / "landscape" 的重力方向结果。
 */
@KNCallback
interface OhosAppWindowBridge {

    /**
     * 设置屏幕方向
     * @param orientation 方向标识："portrait" / "landscape" / "auto"
     */
    fun setScreenOrientation(orientation: String)

    /**
     * 进入全屏模式
     * ArkTS 侧实现：隐藏状态栏和导航栏，应用内容扩展到整个屏幕
     */
    fun enterFullScreen()

    /**
     * 退出全屏模式
     * ArkTS 侧实现：恢复状态栏和导航栏的显示
     */
    fun exitFullScreen()

    /**
     * 获取当前屏幕方向
     * @return 方向标识："portrait" / "landscape"
     */
    fun getScreenOrientation(): String

    /**
     * 开始监听设备横竖屏变化。
     * @param onOrientationChanged 方向变化回调，参数为 "portrait" / "landscape"
     */
    fun startDeviceOrientationListening(
        onOrientationChanged: (orientation: String) -> Unit,
    ): Boolean

    /**
     * 停止监听设备横竖屏变化。
     */
    fun stopDeviceOrientationListening()
}
