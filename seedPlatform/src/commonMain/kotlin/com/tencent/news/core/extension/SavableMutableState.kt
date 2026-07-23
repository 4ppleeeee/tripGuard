package com.tencent.news.core.extension

import com.tencent.news.core.platform.api.appStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 持久化的可变状态容器，能力与 [MutableStateFlow] 保持一致。
 *
 * 每次写入 [value] 时，会通过 [appStorage] 将序列化后的字符串持久化到磁盘；
 * 初始化时会优先从磁盘恢复上次保存的值，若磁盘无记录则使用 [defaultValue]。
 *
 * @param T 状态值类型
 * @param tableName 持久化使用的存储表名
 * @param key 持久化使用的键名
 * @param defaultValue 磁盘无记录时的默认值
 * @param serialize 将 [T] 序列化为字符串的函数
 * @param deserialize 将字符串反序列化为 [T] 的函数，失败时返回 null（此时回退到 [defaultValue]）
 */
class SavableMutableState<T>(
    private val tableName: String,
    private val key: String,
    private val defaultValue: T,
    private val serialize: (T) -> String,
    private val deserialize: (String) -> T?,
) : MutableStateFlow<T> {

    private val delegate: MutableStateFlow<T> = MutableStateFlow(restoreOrDefault())

    // ---- MutableStateFlow / StateFlow ----

    override var value: T
        get() = delegate.value
        set(value) {
            persist(value)
            delegate.value = value
        }

    override val replayCache: List<T>
        get() = delegate.replayCache

    override val subscriptionCount: StateFlow<Int>
        get() = delegate.subscriptionCount

    override fun compareAndSet(expect: T, update: T): Boolean {
        // 仅当 CAS 成功时才持久化
        val success = delegate.compareAndSet(expect, update)
        if (success && expect != update) {
            persist(update)
        }
        return success
    }

    override fun tryEmit(value: T): Boolean {
        persist(value)
        return delegate.tryEmit(value)
    }

    override suspend fun emit(value: T) {
        persist(value)
        delegate.emit(value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun resetReplayCache() {
        delegate.resetReplayCache()
    }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        delegate.collect(collector)
    }

    // ---- 持久化辅助 ----

    private fun restoreOrDefault(): T {
        val raw = appStorage().getKV(tableName, key, "")
        if (raw.isEmpty()) return defaultValue
        return runCatching { deserialize(raw) }.getOrNull() ?: defaultValue
    }

    private fun persist(value: T) {
        appStorage().setKV(tableName, key, serialize(value))
    }

    /** 清除磁盘记录并将状态重置为 [defaultValue]。 */
    fun clear() {
        appStorage().removeValue(tableName, key)
        delegate.value = defaultValue
    }
}

// ---- 便捷工厂函数 ----

/**
 * 创建持久化 [String] 状态。
 */
fun savableStringState(
    tableName: String,
    key: String,
    defaultValue: String = "",
): SavableMutableState<String> = SavableMutableState(
    tableName = tableName,
    key = key,
    defaultValue = defaultValue,
    serialize = { it },
    deserialize = { it },
)

/**
 * 创建持久化 [Int] 状态。
 */
fun savableIntState(
    tableName: String,
    key: String,
    defaultValue: Int = 0,
): SavableMutableState<Int> = SavableMutableState(
    tableName = tableName,
    key = key,
    defaultValue = defaultValue,
    serialize = { it.toString() },
    deserialize = { it.toIntOrNull() },
)

/**
 * 创建持久化 [Long] 状态。
 */
fun savableLongState(
    tableName: String,
    key: String,
    defaultValue: Long = 0L,
): SavableMutableState<Long> = SavableMutableState(
    tableName = tableName,
    key = key,
    defaultValue = defaultValue,
    serialize = { it.toString() },
    deserialize = { it.toLongOrNull() },
)

/**
 * 创建持久化 [Float] 状态。
 */
fun savableFloatState(
    tableName: String,
    key: String,
    defaultValue: Float = 0f,
): SavableMutableState<Float> = SavableMutableState(
    tableName = tableName,
    key = key,
    defaultValue = defaultValue,
    serialize = { it.toString() },
    deserialize = { it.toFloatOrNull() },
)

/**
 * 创建持久化 [Boolean] 状态。
 */
fun savableBooleanState(
    tableName: String,
    key: String,
    defaultValue: Boolean = false,
): SavableMutableState<Boolean> = SavableMutableState(
    tableName = tableName,
    key = key,
    defaultValue = defaultValue,
    serialize = { it.toString() },
    deserialize = { it.toBooleanStrictOrNull() },
)