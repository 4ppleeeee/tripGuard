package com.tencent.news.core.extension


interface IKtList<T> {

    val size: Int

    fun contains(element: T): Boolean
    fun isEmpty(): Boolean

    fun get(index: Int): T
    fun set(index: Int, element: T): T

    fun indexOf(element: T): Int
    fun lastIndexOf(element: T): Int

    fun add(element: T): Boolean
    fun addAt(index: Int, element: T)

    fun addAllList(elements: IKtList<T>): Boolean
    fun addAll(elements: Array<T>): Boolean
    fun addAllAt(index: Int, elements: Array<T>): Boolean

    fun clear()

    fun remove(element: T): Boolean
    fun removeAt(index: Int): T

}


class KtList<T> constructor(val list: MutableList<T>) : IKtList<T>, MutableList<T> by list {

    companion object {
        fun <T> create(data: Array<T>): KtList<T> {
            return KtList(data.toMutableList())
        }
    }

    override fun addAt(index: Int, element: T) {
        list.add(index, element)
    }

    override fun addAll(elements: Array<T>): Boolean {
        return list.addAll(elements.asList())
    }

    override fun addAllAt(index: Int, elements: Array<T>): Boolean {
        return list.addAll(index, elements.asList())
    }

    override fun addAllList(elements: IKtList<T>): Boolean {
        val newList = mutableListOf<T>()
        for (i in 0 until elements.size) {
            newList.add(elements.get(i))
        }
        return list.addAll(newList)
    }

}

