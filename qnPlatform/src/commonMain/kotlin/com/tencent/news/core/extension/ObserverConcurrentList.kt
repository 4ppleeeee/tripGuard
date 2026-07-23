package com.tencent.news.core.extension

import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.synchronized

interface DataChangeCallback {
    fun onDataSetChange(isClean: Boolean)
}

class ObserverConcurrentList<T> : ConcurrentList<T>() {

    private val callbackLock = Lock()

    private val dataChangeList = arrayListOf<DataChangeCallback>()


    fun registerAudioPlayStateChangeListener(changeCallback: DataChangeCallback) {
        synchronized(callbackLock) {
            if (!dataChangeList.contains(changeCallback)) {
                dataChangeList.add(changeCallback)
            }
        }
    }

    fun unRegisterAudioPlayStateChangeListener(changeCallback: DataChangeCallback) {
        synchronized(callbackLock) {
            dataChangeList.remove(changeCallback)
        }
    }

    override fun add(element: T) {
        super.add(element)
        notifyDataSetChange(false)
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        notifyDataSetChange(false)
    }

    override fun clear() {
        super.clear()
        notifyDataSetChange(true)
    }

    override fun clearAndAddAll(data: Collection<T?>?) {
        super.clearAndAddAll(data)
        notifyDataSetChange(true)
    }

    private fun notifyDataSetChange(isClean: Boolean) {
        val callbackSnapshot = synchronized(callbackLock) {
            dataChangeList.toList()
        }
        callbackSnapshot.forEach {
            it.onDataSetChange(isClean)
        }
    }
}
