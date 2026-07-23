package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.PrimitiveMap
import com.tencent.news.core.extension.safeEncodeToJson
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.list.api.IExportModel
import com.tencent.news.core.list.api.IExportModelData
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.qnLogcat
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


interface IEvent : IExportModelData {

    // 给鸿蒙广播的事件名（如果为空发不出去）
    val exportEventName: String get() = ""

    fun exportJson(): String {
        val eventName = exportEventName.takeIfNotEmpty() ?: return ""
        val eventData = buildExportPrimitiveMap().safeEncodeToJson()
        return mapOf(
            IExportModel.KEY_EVENT_NAME to eventName,
            IExportModel.KEY_EVENT_DATA to eventData,
        ).safeEncodeToJson()
    }

}

open class BaseEvent : IEvent {
    override fun buildExportPrimitiveMap(): PrimitiveMap {
        // 目前还没给鸿蒙适配广播，暂时留个空实现；
        // 鸿蒙需要最后转换成json string才行，不认识这个event类
        return mapOf()
    }
}


interface IEventBus {

    fun post(event: IEvent)

    fun postSticky(event: IEvent)

}


fun appEventBus(): IEventBus {
    return QnPlatformLogic.eventBus ?: defaultEventBus
}

private val defaultEventBus by lazy { DefaultEventBus() }

class DefaultEventBus : IEventBus {
    override fun post(event: IEvent) {
    }

    override fun postSticky(event: IEvent) {
    }
}


private val globalEventFlow: MutableSharedFlow<IEvent> = MutableSharedFlow()

interface IEventFlow {
    fun emit(event: IEvent)
    fun collectLastSync(action: (value: IEvent) -> Unit)
    suspend fun collect(collector: FlowCollector<IEvent>)
    suspend fun collectLast(action: suspend (value: IEvent) -> Unit)
}

// 用collect一定注意：他会阻塞协程，一次launch只能collect一个；
// 为了避免这个错误用法，封装了这个带scope的collect方法
inline fun <reified T : IEvent> IEventFlow.safeCollect(
    scope: CoroutineScope,
    collector: FlowCollector<T>
) {
    scope.launch {
        collect { event ->
            if (event is T) {
                collector.emit(event)
            }
        }
    }
}

fun appEventFlow(): IEventFlow {
    return defaultEventFlow
}

private val defaultEventFlow by lazy { DefaultEventFlow() }

class DefaultEventFlow : IEventFlow {
    override fun emit(event: IEvent) {
        GlobalScope.launch {
            globalEventFlow.emit(event)
        }
    }

    override suspend fun collect(collector: FlowCollector<IEvent>) {
        globalEventFlow.collect(collector)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun collectLastSync(action: (value: IEvent) -> Unit) {
        GlobalScope.launch(CoroutineExceptionHandler { _, throwable ->
            if (isDebug()) {
                throw throwable
            } else {
                qnLogcat()?.logE(
                    "IEventFlow",
                    "globalEventFlow.collectLatest(action) 异常",
                    throwable
                )
            }
        }) {
            globalEventFlow.collectLatest(action)
        }
    }

    override suspend fun collectLast(action: suspend (value: IEvent) -> Unit) {
        globalEventFlow.collectLatest(action)
    }

}