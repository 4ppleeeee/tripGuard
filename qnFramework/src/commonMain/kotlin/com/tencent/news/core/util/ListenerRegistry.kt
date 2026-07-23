package com.tencent.news.core.util

import com.tencent.news.core.extension.addIfNotExist
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.synchronized
import com.tencent.news.core.tads.constants.INVALID_NUM

interface IDistinctListener {
    // 如果这个key相同，则只能注册1个实例
    fun getListenerKey(): Int = hashCode()
}

class ListenerRegistry<T> {

    private val lock by lazy { Lock() }

    private val listenerMap by lazy { mutableMapOf<String, MutableList<T>>() }

    fun register(key: String, listener: T) {
        synchronized(lock) {
            val list = getListenerList(key)
            if (listener is IDistinctListener && listener.getListenerKey() != INVALID_NUM) {
                list.addIfNotExist(listener) {
                    (it as? IDistinctListener)?.getListenerKey() == listener.getListenerKey()
                }
            } else {
                list.add(listener)
            }
        }
    }

    fun unRegister(key: String, listener: T) {
        synchronized(lock) {
            getListenerList(key).remove(listener)
        }
    }

    fun find(key: String, condition: (T) -> Boolean): T? {
        synchronized(lock) {
            return getListenerList(key).find(condition)
        }
    }

    fun notify(key: String, action: (T) -> Unit) {
        synchronized(lock) {
            getListenerList(key).forEach(action)
        }
    }

    fun clearListeners(key: String) {
        synchronized(lock) {
            listenerMap.remove(key)
        }
    }

    fun clearAll() {
        synchronized(lock) {
            listenerMap.clear()
        }
    }

    private fun getListenerList(key: String): MutableList<T> {
        synchronized(lock) {
            return listenerMap.getOrPut(key) { mutableListOf() }
        }
    }

}