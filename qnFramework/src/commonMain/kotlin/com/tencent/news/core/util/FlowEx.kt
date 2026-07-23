package com.tencent.news.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object FlowEx {

    // 用collect一定注意：他会阻塞协程，一次launch只能collect一个；
    // 为了避免这个错误用法，封装了这个带scope的collect方法
    // 另外~用这个方法利于减少缩进
    fun <T> Flow<T>.safeCollect(
        scope: CoroutineScope,
        collector: FlowCollector<T>
    ) {
        scope.launch {
            collect(collector)
        }
    }

    fun <T> Flow<T>.safeCollectLatest(
        scope: CoroutineScope,
        action: suspend (value: T) -> Unit
    ) {
        scope.launch {
            collectLatest(action)
        }
    }

}