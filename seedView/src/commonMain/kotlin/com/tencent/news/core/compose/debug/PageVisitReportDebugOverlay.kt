package com.tencent.news.core.compose.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf

/**
 * 页面访问上报调试浮层的注入点。
 *
 * 设计说明：
 * - 浮层完整的 UI 实现由业务调试模块提供，
 *   仅在 debug 包中通过 `wsDebug` 的 `debugImplementation` 引入；
 * - `qnView` 处于通用 UI 层，不直接依赖业务调试代码，因此通过此对象暴露注册入口；
 * - `wsDebug` 启动时调用 [register]，将自身的 `@Composable` 注册到此处；
 * - 主流程（[PageVisitReportDebugOverlay]）直接调用注册的 lambda；release 包未注册时为空，零开销。
 */
object PageVisitReportDebugOverlayHost {

    /**
     * 已注册的浮层 Composable，未注册时为 null。
     *
     * 必须使用 Compose 可观察状态：在 [register] 调用时，已经构建好的 Compose 树（其它存活页面）
     * 才能感知到 overlay 从 null 变为非 null，并自动重组显示浮层；否则其它页面只会读到首次构建时的 null 快照。
     */
    private val overlayState = mutableStateOf<(@Composable () -> Unit)?>(null)

    /**
     * 由 wsDebug 在初始化阶段调用，注册具体的浮层实现。
     */
    fun register(overlay: @Composable () -> Unit) {
        overlayState.value = overlay
    }

    /**
     * 在 Compose 树中调用已注册的浮层；未注册则什么都不做。
     */
    @Composable
    fun Render() {
        overlayState.value?.invoke()
    }
}

/**
 * 页面访问上报调试浮层入口。每个 ComposePage 都会无条件调用此函数；
 * 真正的 UI 由 wsDebug 通过 [PageVisitReportDebugOverlayHost.register] 注入。
 */
@Composable
fun PageVisitReportDebugOverlay() {
    PageVisitReportDebugOverlayHost.Render()
}
