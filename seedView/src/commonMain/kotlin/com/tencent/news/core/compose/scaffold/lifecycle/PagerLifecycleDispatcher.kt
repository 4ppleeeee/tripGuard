package com.tencent.news.core.compose.scaffold.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow
import com.tencent.news.core.platform.qnFileLog
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import com.tencent.news.core.util.lifecycle.PageLifecycleFlow
import kotlinx.coroutines.flow.SharedFlow

private const val DEBUG_TAG = "PageVisitDebug"

/**
 * 通用页面生命周期分发器
 *
 * 参考 StructChannelList.provideLifecycle() 的双维度模式：
 * - **isSelected**：当前页面是否被选中（由外部传入，如 Pager 选中态、Tab 选中态）
 * - **parentResumed**：父页面是否处于前台（自动从 [LocalComposePageLifecycleFlow] 监听）
 *
 * 只有 `isSelected && parentResumed` 同时为 true 时，才处于 RESUMED 状态。
 * 任一条件变为 false，立即分发 ON_PAUSE。
 *
 * 典型使用方式：
 * ```kotlin
 * val childFlow = rememberPageLifecycleDispatcher(isSelected = state.currentPage == index)
 *
 * CompositionLocalProvider(
 *     LocalComposePageLifecycleFlow provides childFlow
 * ) {
 *     ChildContent()
 * }
 * ```
 *
 * @param isSelected 当前页面是否被选中/可见
 * @return 独立的生命周期事件流，子组件通过 [LocalComposePageLifecycleFlow] 消费
 */
@Composable
fun rememberPageLifecycleDispatcher(
    isSelected: Boolean, pagerName: String? = null
): SharedFlow<PageLifecycleEvent> {
    val childFlow = remember { PageLifecycleFlow(pagerName) }

    // 进入 Composition 时发送 ON_CREATE，离开时发送 ON_DESTROY
    DisposableEffect(childFlow) {
        qnFileLog()?.logI(DEBUG_TAG, "[Dispatcher] onCreate pagerName=$pagerName isSelected=$isSelected")
        childFlow.tryEmitEvent(PageLifecycleEvent.ON_CREATE)
        onDispose {
            qnFileLog()?.logI(DEBUG_TAG, "[Dispatcher] onDestroy pagerName=$pagerName")
            childFlow.tryEmitEvent(PageLifecycleEvent.ON_DESTROY)
        }
    }

    // 监听父页面生命周期，维护 parentResumed 状态
    val parentLifecycleFlow = LocalComposePageLifecycleFlow.current
    var parentResumed by remember { mutableStateOf(true) }

    LaunchedEffect(parentLifecycleFlow) {
        qnFileLog()?.logI(
            DEBUG_TAG,
            "[Dispatcher] startCollectParent pagerName=$pagerName parentFlow=${parentLifecycleFlow != null} " +
                "replayLast=${parentLifecycleFlow?.replayCache?.lastOrNull()}"
        )
        parentLifecycleFlow?.collect { event ->
            qnFileLog()?.logI(
                DEBUG_TAG,
                "[Dispatcher] parentEvent pagerName=$pagerName event=$event prevParentResumed=$parentResumed"
            )
            when (event) {
                PageLifecycleEvent.ON_RESUME -> parentResumed = true
                PageLifecycleEvent.ON_PAUSE -> parentResumed = false
                else -> {}
            }
        }
    }

    // 综合 isSelected 和 parentResumed，统一分发 ON_RESUME / ON_PAUSE
    val shouldBeResumed = isSelected && parentResumed
    LaunchedEffect(shouldBeResumed) {
        qnFileLog()?.logI(
            DEBUG_TAG,
            "[Dispatcher] decide pagerName=$pagerName isSelected=$isSelected parentResumed=$parentResumed " +
                "-> shouldBeResumed=$shouldBeResumed"
        )
        if (shouldBeResumed) {
            childFlow.tryEmitEvent(PageLifecycleEvent.ON_RESUME)
        } else {
            childFlow.tryEmitEvent(PageLifecycleEvent.ON_PAUSE)
        }
    }

    return childFlow.lifecycleFlow
}
