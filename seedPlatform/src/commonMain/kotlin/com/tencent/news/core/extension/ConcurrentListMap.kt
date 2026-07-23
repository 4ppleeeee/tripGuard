package com.tencent.news.core.extension

import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.synchronized

/**
 * 线程安全的listMap
 */
class ConcurrentListMap<K, T> {
    // 真正存储数据的内部map
    private val innerMap: MutableMap<K, MutableList<T>> = mutableMapOf()

    // 保护map操作的锁
    private val lock = Lock()

    /**
     * 添加元素，会根据value进行排重
     * @param key 键值
     * @param instance 对应list中元素
     */
    fun add(key: K, instance: T) {
        synchronized(lock) {
            val list = innerMap.getOrPut(key) { mutableListOf() }
            if (!list.contains(instance)) {
                list.add(instance)
            }
        }
    }

    /**
     * 删除元素
     * @param key 键值
     * @param instance 对应list中元素
     */
    fun remove(key: K, instance: T) {
        synchronized(lock) {
            val list = innerMap[key]
            list?.let {
                it.remove(instance)
                if (it.isEmpty()) {
                    innerMap.remove(key)
                }
            }
        }
    }

    /**
     * 遍历删除元素
     * @param instance 对应list中元素
     */
    fun removeValue(instance: T) {
        synchronized(lock) {
            innerMap.values.forEach {
                if (it.contains(instance)) {
                    it.remove(instance)
                }
            }
        }
    }

    /**
     * 获取所有元素
     * @param key 键值
     * @return 元素列表
     */
    fun getAll(key: K): List<T> {
        synchronized(lock) {
            return innerMap[key]?.toList() ?: emptyList()
        }
    }

    /**
     * 执行list的for-each
     * @param key 键值
     * @param action 执行动作
     */
    fun forEach(key: K, action: (T) -> Unit) {
        synchronized(lock) {
            val arr = innerMap[key]
            arr?.toList()?.forEach(action)
        }
    }

    fun getAllKeys(): Set<K> {
        synchronized(lock) {
            return innerMap.keys.toSet()
        }
    }
}