package com.tencent.news.core.extension

interface DataChangeCallback {
    fun onDataSetChange(isClean: Boolean)
}

class ObserverConcurrentList<T> : ConcurrentList<T>() {


    var dataChangeList: ArrayList<DataChangeCallback> = arrayListOf()


    fun registerAudioPlayStateChangeListener(changeCallback: DataChangeCallback) {
        dataChangeList.add(changeCallback)
    }

    fun unRegisterAudioPlayStateChangeListener(changeCallback: DataChangeCallback) {
        dataChangeList.remove(changeCallback)
    }

    override fun add(element: T) {
        super.add(element)
        dataChangeList.forEach {
            it.onDataSetChange(false)
        }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        dataChangeList.forEach {
            it.onDataSetChange(false)
        }
    }

    override fun clear() {
        super.clear()
        dataChangeList.forEach {
            it.onDataSetChange(true)
        }
    }

    override fun clearAndAddAll(data: Collection<T?>?) {
        super.clearAndAddAll(data)
        dataChangeList.forEach {
            it.onDataSetChange(true)
        }
    }
}