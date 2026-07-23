package com.tencent.news.core.platform

/**
 * 线程安全的集合，Android使用ConcurrentHashMap，IOS使用NSCache
 */
actual class ConcurrentMap<K, V> actual constructor() : MutableMap<K, V> {
    private val lock = Lock()

    private val map = hashMapOf<K, V>()

    actual override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            synchronized(lock) {
                return map.entries
            }
        }

    actual override val keys: MutableSet<K>
        get() {
            synchronized(lock) {
                return map.keys
            }
        }

    actual override val size: Int
        get() = map.size

    actual override val values: MutableCollection<V>
        get() {
            synchronized(lock) {
                return map.values
            }
        }

    actual override fun clear() {
        synchronized(lock) {
            map.clear()
        }
    }

    actual override fun isEmpty(): Boolean {
        synchronized(lock) {
            return map.isEmpty()
        }
    }

    actual override fun remove(key: K): V? {
        synchronized(lock) {
            return map.remove(key)
        }
    }

    actual override fun putAll(from: Map<out K, V>) {
        synchronized(lock) {
            map.putAll(from)
        }
    }

    actual override fun put(key: K, value: V): V? {
        synchronized(lock) {
            return map.put(key, value)
        }
    }

    actual override fun get(key: K): V? {
        synchronized(lock) {
            return map.get(key)
        }
    }

    actual override fun containsValue(value: V): Boolean {
        synchronized(lock) {
            return map.containsValue(value)
        }
    }

    actual override fun containsKey(key: K): Boolean {
        synchronized(lock) {
            return map.containsKey(key)
        }
    }
}