package com.tencent.news.core.pop

import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.api.appTask
import com.tencent.news.core.platform.getElapsedRealtime
import com.tencent.news.core.platform.synchronized
import kotlin.math.max

internal data class PopTaskQueueContext(
    val name: String,
    val lock: Lock = Lock(),
    val nowProvider: () -> Long = ::getElapsedRealtime,
    val scheduler: (delayMs: Long, action: () -> Unit) -> Unit = { delayMs, action ->
        appTask().postAction(action, delayMs)
    }
)

internal data class PopTaskQueueConfig<T>(
    val keyProvider: (T) -> String,
    val readyTimeProvider: (entry: T, now: Long) -> Long?,
    val readyComparator: Comparator<T>,
    val canSchedule: () -> Boolean = { true },
    val onReady: (T) -> PopTaskQueueDrainDecision
)

internal data class PopTaskQueueEnqueueResult<T>(
    val enqueued: Boolean,
    val replaced: T?
)

internal enum class PopTaskQueueDrainDecision {
    CONTINUE,
    STOP
}

/**
 * 业务无关的任务队列控制器。它只维护 pending 容器、线程安全和延迟调度；
 * 任务何时可出队、出队后的业务动作，以及失败/取消语义都由外层 manager 决定。
 */
internal class PopTaskQueueController<T>(
    private val context: PopTaskQueueContext,
    private val config: PopTaskQueueConfig<T>
) {

    private val pendingTasks = mutableMapOf<String, T>()
    private var generation = 0L
    private var disposed = false

    fun isDisposed(): Boolean = synchronized(context.lock) { disposed }

    fun pendingCount(): Int = synchronized(context.lock) { pendingTasks.size }

    fun enqueue(entry: T): PopTaskQueueEnqueueResult<T> = synchronized(context.lock) {
        if (disposed) {
            return@synchronized PopTaskQueueEnqueueResult(enqueued = false, replaced = null)
        }
        val key = config.keyProvider(entry)
        val replaced = pendingTasks.put(key, entry)
        PopTaskQueueEnqueueResult(enqueued = true, replaced = replaced)
    }

    fun clear(): List<T> = synchronized(context.lock) {
        if (disposed) return@synchronized emptyList()
        generation += 1
        pendingTasks.values.toList().also { pendingTasks.clear() }
    }

    fun dispose(): List<T> = synchronized(context.lock) {
        if (disposed) return@synchronized emptyList()
        disposed = true
        generation += 1
        pendingTasks.values.toList().also { pendingTasks.clear() }
    }

    fun drainReady() {
        while (true) {
            val entry = dequeueReady() ?: run {
                scheduleNext()
                return
            }
            when (config.onReady(entry)) {
                PopTaskQueueDrainDecision.CONTINUE -> Unit
                PopTaskQueueDrainDecision.STOP -> return
            }
        }
    }

    fun scheduleNext() {
        val now = context.nowProvider()
        val schedule = synchronized(context.lock) {
            generation += 1
            if (disposed || !config.canSchedule()) {
                return@synchronized null
            }
            val token = generation
            val readyTime = pendingTasks.values
                .mapNotNull { config.readyTimeProvider(it, now) }
                .minOrNull()
            readyTime?.let { token to max(0L, it - now) }
        } ?: return

        context.scheduler(schedule.second) {
            val isCurrent = synchronized(context.lock) { schedule.first == generation }
            if (isCurrent) {
                drainReady()
            }
        }
    }

    private fun dequeueReady(): T? {
        val now = context.nowProvider()
        return synchronized(context.lock) {
            if (disposed) return@synchronized null
            pendingTasks.values
                .filter { entry ->
                    config.readyTimeProvider(entry, now)?.let { readyTime -> readyTime <= now } == true
                }
                .sortedWith(config.readyComparator)
                .firstOrNull()
                ?.also { pendingTasks.remove(config.keyProvider(it)) }
        }
    }
}
