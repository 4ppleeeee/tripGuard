package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

/**
 * 屏幕显示环境信息。
 *
 * 只承担读取屏幕尺寸、密度等显示信息，不负责窗口控制或 App 状态。
 */
interface IScreenInfo {
    fun getScreenWidth(): Int
    fun getScreenHeight(): Int
    fun getScreenWidthInch(): Float
    fun getScreenHeightInch(): Float
    fun getDpi(): Int
}

fun appScreenInfo(): IScreenInfo {
    return QnPlatformLogic.screenInfo ?: appStatusScreenInfo
}

private val appStatusScreenInfo by lazy { AppStatusScreenInfo() }

@Suppress("DEPRECATION")
private class AppStatusScreenInfo : IScreenInfo {
    override fun getScreenWidth(): Int = appStatus().getScreenWidth()
    override fun getScreenHeight(): Int = appStatus().getScreenHeight()
    override fun getScreenWidthInch(): Float = appStatus().getScreenWidthInch()
    override fun getScreenHeightInch(): Float = appStatus().getScreenHeightInch()
    override fun getDpi(): Int = appStatus().getDpi()
}
