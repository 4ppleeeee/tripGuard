package com.tencent.news.core.util.lifecycle

import com.tencent.news.core.list.trace.LifecycleLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 页面生命周期事件类型
 */
enum class PageLifecycleEvent {
    ON_CREATE,
    ON_RESUME,
    ON_PAUSE,
    ON_DESTROY
}

/**
 * 页面生命周期流管理类，用于向子组件传递页面生命周期事件
 */
class PageLifecycleFlow {
    private val _lifecycleFlow =
        MutableSharedFlow<PageLifecycleEvent>(extraBufferCapacity = 1, replay = 1)
    val lifecycleFlow: SharedFlow<PageLifecycleEvent> = _lifecycleFlow.asSharedFlow()

    /**
     * 发送生命周期事件
     */
    suspend fun emitEvent(event: PageLifecycleEvent) {
        LifecycleLog.debug("PageLifecycleFlow") { "$event" }
        _lifecycleFlow.emit(event)
    }

    /**
     * 同步发送生命周期事件（不等待）
     */
    fun tryEmitEvent(event: PageLifecycleEvent) {
        LifecycleLog.debug("PageLifecycleFlow") { "$event" }
        _lifecycleFlow.tryEmit(event)
    }
} 