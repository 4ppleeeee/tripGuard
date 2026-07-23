package com.tencent.news.core.util.lifecycle

import com.tencent.news.core.platform.qnLogcat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 页面安全区刷新事件流，用于向子组件传递宿主重新计算后的 safeAreaInsets。
 */
class PageSafeAreaInsetsFlow {
    private val _safeAreaInsetsFlow = MutableSharedFlow<Map<String, Any>>(extraBufferCapacity = 1)
    val safeAreaInsetsFlow: SharedFlow<Map<String, Any>> = _safeAreaInsetsFlow.asSharedFlow()

    fun tryEmitSafeAreaInsets(data: Map<String, Any>): Boolean {
        qnLogcat()?.logD("PageSafeAreaInsetsFlow", "tryEmit: $data")
        return _safeAreaInsetsFlow.tryEmit(data)
    }
}
