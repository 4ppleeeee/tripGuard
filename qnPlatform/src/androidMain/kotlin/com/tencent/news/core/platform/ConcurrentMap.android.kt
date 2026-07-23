package com.tencent.news.core.platform

import java.util.concurrent.ConcurrentHashMap

/**
 * Android的ConcurrentHashMap是线程安全的map
 */
actual class ConcurrentMap<K, V> actual constructor() : MutableMap<K, V> {

    private val delegate = ConcurrentHashMap<K, V>()

    actual override val size: Int get() = delegate.size
    actual override val keys: MutableSet<K> get() = delegate.keys
    actual override val values: MutableCollection<V> get() = delegate.values
    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = delegate.entries
    actual override fun isEmpty(): Boolean = delegate.isEmpty()
    actual override fun containsKey(key: K): Boolean = delegate.containsKey(key)
    actual override fun containsValue(value: V): Boolean = delegate.containsValue(value)
    actual override fun get(key: K): V? = delegate[key]
    actual override fun put(key: K, value: V): V? = delegate.put(key, value)
    actual override fun remove(key: K): V? = delegate.remove(key)
    actual override fun putAll(from: Map<out K, V>) = delegate.putAll(from)
    actual override fun clear() = delegate.clear()
}