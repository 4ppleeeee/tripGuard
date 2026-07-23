package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IStatusBarController
import com.tencent.news.core.platform.qnLogcat
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppStatusBar = JSValue

/**
 * 注入鸿蒙端 [IStatusBarController] 实现。
 *
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧基于 @kit.ArkUI 的
 * window.setWindowSystemBarProperties 真实实现桥接到 KMP 层。
 *
 * ArkTS 侧通过 Kommon.setup 调用 getHarmonyStartupProvider().setupAppStatusBar(new
 * OhosAppStatusBarCallback()) 注入实现。
 */
fun setupOhosAppStatusBar(statusBar: IOhosAppStatusBar) {
    QnPlatformLogic.statusBarController = OhosStatusBarControllerProvider(statusBar.asOhosAppStatusBar())
}

/**
 * Kotlin 侧的 [IStatusBarController] 实现：所有方法转发给 ArkTS 侧实现。
 *
 * - setWhiteBar / setBlackBar / resetStatusBar 对应 ArkTS 侧设置 statusBarContentColor
 *   为白色/黑色/系统默认；
 * - setStatusBarVisibility 对应 ArkTS 侧设置状态栏显示/隐藏；
 * - setCustomBar 支持业务自定义状态栏文字/背景色。
 *
 * 鸿蒙端状态栏设置需要 Window 实例（通过 windowStage.getMainWindow 获取），
 * 因此具体实现放在 ArkTS 侧完成。
 */
private class OhosStatusBarControllerProvider(
    private val native: OhosAppStatusBar,
) : IStatusBarController {

    private companion object {
        const val TAG = "OhosAppStatusBar"
    }

    override fun setWhiteBar() {
        runCatching { native.setWhiteBar() }
            .onFailure { qnLogcat()?.logE(TAG, "setWhiteBar failed", it) }
    }

    override fun setBlackBar() {
        runCatching { native.setBlackBar() }
            .onFailure { qnLogcat()?.logE(TAG, "setBlackBar failed", it) }
    }

    override fun resetStatusBar() {
        runCatching { native.resetStatusBar() }
            .onFailure { qnLogcat()?.logE(TAG, "resetStatusBar failed", it) }
    }

    override fun setCustomBar(textColor: String, backgroundColor: String) {
        runCatching { native.setCustomBar(textColor, backgroundColor) }
            .onFailure { qnLogcat()?.logE(TAG, "setCustomBar failed", it) }
    }

    override fun setStatusBarVisibility(visible: Boolean) {
        runCatching { native.setStatusBarVisibility(visible) }
            .onFailure { qnLogcat()?.logE(TAG, "setStatusBarVisibility failed", it) }
    }
}

/**
 * ArkTS 侧状态栏能力实现接口。
 *
 * knoi 编译时会自动生成 ArkTS 侧的接口定义，ArkTS 侧 OhosAppStatusBarCallback
 * 实现该接口并通过 getHarmonyStartupProvider().setupAppStatusBar 注入。
 *
 * 设计要点：
 *  - setWhiteBar / setBlackBar / resetStatusBar 为常用快捷方法，
 *    分别对应白字（深色沉浸式背景）、黑字（浅色背景）、恢复系统默认；
 *  - setStatusBarVisibility 控制状态栏显示/隐藏；
 *  - setCustomBar 让业务侧可以传任意颜色（#RRGGBB 字符串），兼容腾讯新闻中
 *    自定义品牌色状态栏场景。
 */
@KNCallback
interface OhosAppStatusBar {

    /** 设置为白色图标/文字（通常用于深色背景） */
    fun setWhiteBar()

    /** 设置为黑色图标/文字（通常用于浅色背景） */
    fun setBlackBar()

    /** 恢复系统默认状态栏 */
    fun resetStatusBar()

    /** 设置状态栏显示/隐藏 */
    fun setStatusBarVisibility(visible: Boolean)

    /**
     * 自定义状态栏。
     *
     * @param textColor 状态栏文字/图标颜色（#RRGGBB）
     * @param backgroundColor 状态栏背景色（#RRGGBB）
     */
    fun setCustomBar(textColor: String, backgroundColor: String)
}
