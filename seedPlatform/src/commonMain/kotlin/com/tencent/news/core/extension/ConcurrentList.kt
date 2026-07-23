package com.tencent.news.core.extension

import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.synchronized


// 线程安全的list（加锁了）
open class ConcurrentList<T> {

    private val lock = Lock()

    private val list = ArrayList<T>()

    val size: Int
        get() = synchronized(lock) {
            list.size
        }


    open fun add(element: T) {
        synchronized(lock) {
            list.add(element)
        }
    }

    open fun add(index: Int, element: T) {
        synchronized(lock) {
            list.add(index, element)
        }
    }


    fun <R> map(transform: (T) -> R): List<R> {
        synchronized(lock) {
            return list.map(transform)
        }
    }

    fun contains(element: T): Boolean {
        synchronized(lock) {
            return list.contains(element)
        }
    }

    fun remove(element: T) {
        synchronized(lock) {
            list.remove(element)
        }
    }

    /**
     * 移除满足条件的所有元素
     * @param predicate 判断条件
     * @return 是否有元素被移除
     */
    fun removeIf(predicate: (T) -> Boolean): Boolean {
        synchronized(lock) {
            return list.removeAll(predicate)
        }
    }

    fun removeLastOrNull(): T? {
        synchronized(lock) {
            return list.removeLastOrNull()
        }
    }

    fun forEach(action: (listener: T) -> Unit) {
        synchronized(lock) {
            list.toList().forEach { action(it) }
        }
    }

    fun find(condition: (listener: T) -> Boolean): T? {
        synchronized(lock) {
            return list.find { condition(it) }
        }
    }

    fun firstOrNull(): T? {
        synchronized(lock) {
            return list.firstOrNull()
        }
    }

    fun firstOrNull(predicate: (T) -> Boolean): T? {
        synchronized(lock) {
            return list.firstOrNull(predicate)
        }
    }

    fun safeGet(index: Int): T? {
        synchronized(lock) {
            return list.safeGet(index)
        }
    }

    open fun clear() {
        synchronized(lock) {
            list.clear()
        }
    }

    fun isEmpty(): Boolean {
        synchronized(lock) {
            return list.isEmpty()
        }
    }

    fun isNotEmpty(): Boolean {
        synchronized(lock) {
            return list.isNotEmpty()
        }
    }

    open fun clearAndAddAll(data: Collection<T?>?) {
        synchronized(lock) {
            list.clearAndAddAll(data)
        }
    }

    fun indexOfFirst(predicate: (T) -> Boolean): Int {
        synchronized(lock) {
            return list.indexOfFirst(predicate)
        }
    }

    fun any(predicate: (T) -> Boolean): Boolean {
        synchronized(lock) {
            return list.any(predicate)
        }
    }

    fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        synchronized(lock) {
            return list.subList(fromIndex, toIndex)
        }
    }

    fun lastOrNull(): T? {
        synchronized(lock) {
            return list.lastOrNull()
        }
    }

    fun shallowCopyList(): ArrayList<T> {
        synchronized(lock) {
            return ArrayList(list)
        }
    }

}