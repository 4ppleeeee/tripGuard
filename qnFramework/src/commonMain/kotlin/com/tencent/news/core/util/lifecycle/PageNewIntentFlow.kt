package com.tencent.news.core.util.lifecycle

import com.tencent.news.core.list.trace.IntentLog
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 页面 NewIntent 事件流管理类，用于向子组件传递页面 NewIntent 事件
 * 主要用于处理页面重新打开时的参数传递
 */
class PageNewIntentFlow {
    private val _newIntentFlow = MutableSharedFlow<Map<String, Any>>(extraBufferCapacity = 1)
    val newIntentFlow: SharedFlow<Map<String, Any>> = _newIntentFlow.asSharedFlow()

    /**
     * 发送 NewIntent 事件（挂起函数版本）
     * @param data 需要传递的数据
     */
    suspend fun emitNewIntent(data: Map<String, Any>) {
        IntentLog.debug("PageNewIntentFlow") { "emit: $data" }
        _newIntentFlow.emit(data)
    }

    /**
     * 发送 NewIntent 事件（非挂起函数版本）
     * @param data 需要传递的数据
     * @return 是否发送成功
     */
    fun tryEmitNewIntent(data: Map<String, Any>): Boolean {
        IntentLog.debug("PageNewIntentFlow") { "tryEmit: $data" }
        return _newIntentFlow.tryEmit(data)
    }
}