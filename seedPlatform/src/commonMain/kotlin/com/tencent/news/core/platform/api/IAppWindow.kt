package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

/**
 * 屏幕方向
 */
enum class ScreenOrientation {
    /** 竖屏 */
    PORTRAIT,
    /** 横屏 */
    LANDSCAPE,
    /** 跟随系统自动旋转 */
    AUTO
}

/**
 * 窗口管理接口，提供屏幕常亮、屏幕方向、全屏等窗口级别的控制能力
 */
interface IAppWindow {

    /**
     * 保持屏幕常亮
     */
    fun keepScreenOn()

    /**
     * 取消屏幕常亮，恢复系统默认的息屏策略
     */
    fun cancelScreenOn()

    /**
     * 设置屏幕方向
     * @param orientation 目标屏幕方向
     */
    fun setScreenOrientation(orientation: ScreenOrientation)

    /**
     * 获取当前屏幕方向
     * @return 当前屏幕方向
     */
    fun getScreenOrientation(): ScreenOrientation

    /**
     * 进入全屏模式
     * 隐藏状态栏和底部系统导航栏
     */
    fun enterFullScreen()

    /**
     * 退出全屏模式
     * 恢复系统栏显示，并保持 Activity 的 edge-to-edge 沉浸布局
     */
    fun exitFullScreen()
}

/**
 * 设备方向变化监听器。
 *
 * 该能力用于监听重力感应触发的横竖屏变化，平台实现应在系统旋转锁定时停止分发回调。
 */
fun interface IDeviceOrientationListener {
    fun onDeviceOrientationChanged(orientation: ScreenOrientation)
}

/**
 * appWindow 的可选重力方向感应能力。
 *
 * 独立于 [IAppWindow] 主接口，避免要求未接入该能力的平台宿主立刻补齐实现。
 */
interface IAppWindowOrientationSensor {
    fun registerDeviceOrientationListener(listener: IDeviceOrientationListener): Boolean
    fun unregisterDeviceOrientationListener(listener: IDeviceOrientationListener)
    fun unregisterAllDeviceOrientationListeners()

    /**
     * 查询系统自动旋转是否开启（屏幕旋转未锁定）。
     *
     * 业务方可在收到 [IDeviceOrientationListener.onDeviceOrientationChanged] 回调后，
     * 自行调用此方法决定是否响应本次旋转变化。
     *
     * @return true 表示系统自动旋转开启，false 表示旋转已被用户锁定
     */
    fun isAutoRotationEnabled(): Boolean
}

fun IAppWindow.registerDeviceOrientationListener(listener: IDeviceOrientationListener): Boolean {
    return (this as? IAppWindowOrientationSensor)?.registerDeviceOrientationListener(listener) ?: false
}

fun IAppWindow.unregisterDeviceOrientationListener(listener: IDeviceOrientationListener) {
    (this as? IAppWindowOrientationSensor)?.unregisterDeviceOrientationListener(listener)
}

fun IAppWindow.unregisterAllDeviceOrientationListeners() {
    (this as? IAppWindowOrientationSensor)?.unregisterAllDeviceOrientationListeners()
}

/**
 * 查询系统自动旋转是否开启。
 *
 * 若平台未实现 [IAppWindowOrientationSensor]，默认返回 true（允许旋转）。
 */
fun IAppWindow.isAutoRotationEnabled(): Boolean {
    return (this as? IAppWindowOrientationSensor)?.isAutoRotationEnabled() ?: true
}
/**
 * 底部导航栏控制能力。
 *
 * 作为可选接口单独声明，避免扩大 iOS/鸿蒙现有 IAppWindow 实现的协议面。
 */
interface INavigationBarWindow {
    fun setNavigationBarVisibility(visible: Boolean)

    fun setNavigationBarDarkButtons(isDark: Boolean)

    fun getNavigationBarHeight(): Int
}


fun appWindow(): IAppWindow = QnPlatformLogic.appWindow ?: emptyImpl

fun IAppWindow.isLandscape(): Boolean {
    return getScreenOrientation() == ScreenOrientation.LANDSCAPE
}

private val emptyImpl by lazy { DefaultAppWindow() }

open class DefaultAppWindow : IAppWindow {
    override fun keepScreenOn() {}
    override fun cancelScreenOn() {}
    override fun setScreenOrientation(orientation: ScreenOrientation) {}
    override fun getScreenOrientation(): ScreenOrientation = ScreenOrientation.PORTRAIT
    override fun enterFullScreen() {}
    override fun exitFullScreen() {}
}
