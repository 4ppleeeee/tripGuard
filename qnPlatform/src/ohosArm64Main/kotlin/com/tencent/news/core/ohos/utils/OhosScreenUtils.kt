package com.tencent.news.core.ohos.utils

import platform.framework.OH_NativeDisplayManager_GetDefaultDisplayDensityDpi
import platform.framework.OH_NativeDisplayManager_GetDefaultDisplayDensityXdpi
import platform.framework.OH_NativeDisplayManager_GetDefaultDisplayDensityYdpi
import platform.framework.OH_NativeDisplayManager_GetDefaultDisplayHeight
import platform.framework.OH_NativeDisplayManager_GetDefaultDisplayWidth

/**
 * 鸿蒙屏幕工具类
 * 提供屏幕尺寸、DPI等信息的获取能力
 */
object OhosScreenUtils {

    /**
     * 获取屏幕DPI
     * @return 屏幕DPI值，获取失败返回0
     */
    internal fun getScreenDpi(): Int = getInt {
        OH_NativeDisplayManager_GetDefaultDisplayDensityDpi(it)
    }

    /**
     * 获取屏幕宽度（像素）
     * @return 屏幕宽度，获取失败返回0
     */
    internal fun getScreenWidth(): Int = getInt {
        OH_NativeDisplayManager_GetDefaultDisplayWidth(it)
    }

    /**
     * 获取屏幕高度（像素）
     * @return 屏幕高度，获取失败返回0
     */
    internal fun getScreenHeight(): Int =
        getInt { OH_NativeDisplayManager_GetDefaultDisplayHeight(it) }

    /**
     * 获取设备宽度（DP）
     * @return 设备宽度（DP），保留2位小数，获取失败返回0
     */
    internal fun getDeviceWidthDp(): Float {
        val screenWidth = getScreenWidth()
        if (screenWidth == 0) return 0F

        val xdpi = getFloat { OH_NativeDisplayManager_GetDefaultDisplayDensityXdpi(it) }
        val result = if (xdpi != 0f) (screenWidth.toFloat() / xdpi) else 0F

        return (result * 100).toInt() / 100f
    }

    /**
     * 获取设备高度（DP）
     * @return 设备高度（DP），保留2位小数，获取失败返回0
     */
    internal fun getDeviceHeightDp(): Float {
        val screenHeight = getScreenHeight()
        if (screenHeight == 0) return 0F

        val ydpi = getFloat { OH_NativeDisplayManager_GetDefaultDisplayDensityYdpi(it) }
        val result = if (ydpi != 0f) (screenHeight.toFloat() / ydpi) else 0F

        return (result * 100).toInt() / 100f
    }
}