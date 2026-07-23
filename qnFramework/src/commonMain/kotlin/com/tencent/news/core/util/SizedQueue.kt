package com.tencent.news.core.util


class SizedQueue<T>(
    private val sizeLimit: Int = NO_LIMIT_SIZE,
    private val dequeueWhenOverflow: Boolean = false,    // 配合 sizeLimit 使用，当个数溢出时是否要dequeue并添加新数据
) : IQueue<T> {

    private val dataList = mutableListOf<T>()

    override fun enqueue(value: T): Boolean {
        if (sizeLimit <= 0 || size() < sizeLimit) {
            dataList.add(value)
            return true
        } else {
            if (dequeueWhenOverflow) {
                dequeue()
                dataList.add(value)
                return true
            } else {
                return false
            }
        }
    }

    override fun dequeue(): T? = dataList.removeFirstOrNull()

    override fun peek(): T? = dataList.firstOrNull()

    override fun size(): Int = dataList.size

    companion object {
        const val NO_LIMIT_SIZE = -1
    }
}