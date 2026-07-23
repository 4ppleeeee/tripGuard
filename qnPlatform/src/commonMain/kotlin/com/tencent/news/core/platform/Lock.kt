package com.tencent.news.core.platform


// 分平台实现加锁机制
class Lock() {

    private val lock by lazy {
        kotlinx.atomicfu.locks.reentrantLock()
    }

    fun lock() {
        lock.lock()
    }

    fun unlock() {
        lock.unlock()
    }

}

inline fun <T> synchronized(lock: Lock, action: () -> T): T {
    lock.lock()
    try {
        return action() // 执行需要互斥访问的代码
    } finally {
        lock.unlock()
    }
}