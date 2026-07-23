package com.tencent.news.core.vm.holder

import com.tencent.news.core.extension.IKmmKeep


// IVMHolder 系列命名的，都是可空的
// IVMHolder2 系列命名的，都是非空的

interface IVMHolder<T> : IKmmKeep {
    fun createOrGet(): T?
    fun reCreate(): T?
}

operator fun <T> IVMHolder<T>?.invoke(): T? = this?.createOrGet() // 简化调用方式

interface IVMHolder2<T> : IVMHolder<T> {
    override fun createOrGet(): T
    override fun reCreate(): T
}

operator fun <T> IVMHolder2<T>.invoke(): T = createOrGet() // 简化调用方式


// 带入参的VMHolder，返回值可能空
interface IVMHolder3<D, T> : IKmmKeep {
    fun createOrGet(data: D): T?
    fun reCreate(data: D): T?
}

interface IVMHolder4<D, T> : IVMHolder3<D, T> {
    override fun createOrGet(data: D): T
    override fun reCreate(data: D): T
}

interface IVMArrayHolder<T> : IKmmKeep {
    fun createOrGet(): Array<T>?
    fun reCreate(): Array<T>?
}

interface IVMArrayHolder2<T> : IVMArrayHolder<T> {
    override fun createOrGet(): Array<T>
    override fun reCreate(): Array<T>
}

class VMHolder<T>(private val creator: () -> T?) : IVMHolder<T?> {

    private var instance: T? = null

    override fun createOrGet(): T? {
        val result = instance ?: creator()
        instance = result
        return result
    }

    override fun reCreate(): T? {
        val result = creator()
        instance = result
        return result
    }

}

class VMHolder2<T>(private val creator: () -> T) : IVMHolder2<T> {

    private var instance: T? = null

    override fun createOrGet(): T {
        val result = instance ?: creator()
        instance = result
        return result
    }

    override fun reCreate(): T {
        val result = creator()
        instance = result
        return result
    }

}

// 带入参的VMHolder
class VMHolder3<D, T>(private val creator: (D) -> T?) : IVMHolder3<D, T?> {

    // 根据入参data的hashCode缓存
    private val instance by lazy { mutableMapOf<Int, T?>() }

    override fun createOrGet(data: D): T? {
        val result = instance[data.hashCode()] ?: creator(data)
        instance[data.hashCode()] = result
        return result
    }

    override fun reCreate(data: D): T? {
        val result = creator(data)
        instance[data.hashCode()] = result
        return result
    }

}

class VMHolder4<D, T>(private val creator: (D) -> T) : IVMHolder4<D, T> {

    // 根据入参data的hashCode缓存
    private val instance by lazy { mutableMapOf<Int, T>() }

    override fun createOrGet(data: D): T {
        val result = instance[data.hashCode()] ?: creator(data)
        instance[data.hashCode()] = result
        return result
    }

    override fun reCreate(data: D): T {
        val result = creator(data)
        instance[data.hashCode()] = result
        return result
    }

}

class VMArrayHolder<T>(private val creator: () -> Array<T>?) : IVMArrayHolder<T> {

    private var instance: Array<T>? = null

    override fun createOrGet(): Array<T>? {
        val result = instance ?: creator()
        instance = result
        return result
    }

    override fun reCreate(): Array<T>? {
        val result = creator()
        instance = result
        return result
    }

}


class VMArrayHolder2<T>(private val creator: () -> Array<T>) : IVMArrayHolder2<T> {

    private var instance: Array<T>? = null

    override fun createOrGet(): Array<T> {
        val result = instance ?: creator()
        instance = result
        return result
    }

    override fun reCreate(): Array<T> {
        val result = creator()
        instance = result
        return result
    }

}

interface IViewHolderCreator<Data, ViewHolder> {
    fun checkAndCreate(data: Data): ViewHolder?
}

inline fun <Data, ViewHolder> IViewHolderCreator<Data, ViewHolder>.toVH(data: Data) =
    VMHolder {
        checkAndCreate(data)
    }
