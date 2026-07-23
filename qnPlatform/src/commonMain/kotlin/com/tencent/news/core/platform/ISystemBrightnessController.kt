package com.tencent.news.core.platform

/**
 * 系统（当前窗口）亮度控制器
 *
 * 对齐 Android: [com.tencent.news.qnplayer.VideoBrightnessManager]
 * - 读/写当前 Activity 的 `Window.attributes.screenBrightness`（仅当前 Activity 生效，不改系统全局亮度）
 * - 未设置 Window 亮度时（`BRIGHTNESS_OVERRIDE_NONE`）返回系统当前亮度
 *
 * 说明：由平台侧在 setup 阶段注入 [QnPlatformLogic.systemBrightnessController]。
 */
interface ISystemBrightnessController {
    /** 获取当前亮度比例 [0f, 1f] */
    fun getBrightness(): Float

    /** 设置当前窗口亮度 [0f, 1f] */
    fun setBrightness(value: Float)

    /** 重置窗口亮度，回退到系统亮度（对齐 `VideoBrightnessManager.resetWindowBrightness()`） */
    fun reset()
}

fun appBrightnessController() = QnPlatformLogic.systemBrightnessController
