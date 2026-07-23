package com.tencent.news.core.util


interface IQueue<T> {

    // 返回true：代表入队列成功（如果队列支持了限制个数，这里是可能false的）
    fun enqueue(value: T): Boolean

    fun dequeue(): T?   // 出队列一个元素（会remove）
    fun peek(): T?      // 查询队列首个元素（仅查询，不会remove）

    fun size(): Int

}