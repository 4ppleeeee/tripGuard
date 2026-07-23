package com.tencent.news.core.tads.tab2.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * 控制器三态：
 * - [INITIAL]：刚进入页面，尚未触摸
 * - [SHOWN]：触摸后主动展示（所有受控组件可见）
 * - [HIDDEN]：再次触摸或 auto-hide 隐藏（所有受控组件不可见）
 *
 * 流转：`INITIAL → (tap) → SHOWN → (tap/auto-hide) → HIDDEN → (tap) → SHOWN → ...`
 */
enum class ControllerState {
    INITIAL,
    SHOWN,
    HIDDEN,
}

/**
 * 全屏 overlay 触摸显隐控制器 VM
 *
 * 对标原生 PlayerControllerExecutor 的 showOrHide() + delayHide() 行为：
 * - 点击全屏区域 → toggle UI 显隐
 * - 显示后 [AUTO_HIDE_MS] 毫秒自动隐藏
 *
 * 三态模型：
 * - INITIAL：刚进入页面。`showOnInit=true` 的组件可见，`showOnInit=false` 的隐藏
 * - SHOWN：触摸后。所有受控组件可见
 * - HIDDEN：再次触摸 / auto-hide。所有受控组件隐藏
 *
 * Compose 侧通过 `ControlledVisibility` 扩展声明式包裹：
 * ```kotlin
 * controllerVM.ControlledVisibility { InfoPanel(...) }                      // showOnInit=true（默认）
 * controllerVM.ControlledVisibility(showOnInit = false) { BackButton(...) } // 初始隐藏，触摸后出现
 * VideoPlayer(...)                                                          // 不包裹 = 始终可见
 * ```
 */
interface IAdFullScreenControllerVM {

    /** 控制器当前状态 */
    val state: StateFlow<ControllerState>
    fun show()
    fun hide()

    fun onTapScreen()

    fun startAutoHide(scope: CoroutineScope)
    fun cancelAutoHide()
    fun reset()

    companion object {
        const val AUTO_HIDE_MS = 3000L
    }
}
