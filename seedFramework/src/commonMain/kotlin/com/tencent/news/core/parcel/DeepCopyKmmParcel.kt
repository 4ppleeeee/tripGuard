package com.tencent.news.core.parcel

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.util.IQueue
import com.tencent.news.core.util.SizedQueue
import kotlin.reflect.KClass
import kotlin.reflect.safeCast


// 借用parcel机制，在内存中clone对象时使用
class DeepCopyKmmParcel(private val enableDeepCopy: Boolean = true) : IKmmParcel {

    private val parcelDataQueue: IQueue<Any?> = SizedQueue()

    override fun writeInt(value: Int) {
        parcelDataQueue.enqueue(value)
    }

    override fun readInt(): Int = parcelDataQueue.dequeue().safeCast<Int>() ?: 0

    override fun writeLong(value: Long) {
        parcelDataQueue.enqueue(value)
    }

    override fun readLong(): Long = parcelDataQueue.dequeue().safeCast<Long>() ?: 0

    override fun writeFloat(value: Float) {
        parcelDataQueue.enqueue(value)
    }

    override fun readFloat(): Float = parcelDataQueue.dequeue().safeCast<Float>() ?: 0f

    override fun writeDouble(value: Double) {
        parcelDataQueue.enqueue(value)
    }

    override fun readDouble(): Double = parcelDataQueue.dequeue().safeCast<Double>() ?: 0.0

    override fun writeString(value: String?) {
        parcelDataQueue.enqueue(value.getNonNull())
    }

    override fun readString(): String = parcelDataQueue.dequeue().safeCast<String>() ?: ""

    override fun writeBoolean(value: Boolean) {
        parcelDataQueue.enqueue(value)
    }

    override fun readBoolean(): Boolean = parcelDataQueue.dequeue().safeCast<Boolean>() ?: false

    override fun writeSerializable(value: IKmmKeep?) {
        // todo genesisli dev: 考虑deepCopy？
        parcelDataQueue.enqueue(value)
    }

    override fun readSerializable(): IKmmKeep? = parcelDataQueue.dequeue().safeCast<IKmmKeep>()

    override fun writeParcelable(value: IKmmParcelable?) {
        parcelDataQueue.enqueue(value.checkDeepCopy())
    }

    override fun <T : IKmmParcelable> readParcelable(clazz: KClass<T>, dest: () -> T): T? =
        clazz.safeCast(parcelDataQueue.dequeue())

    override fun writeParcelList(value: List<IKmmParcelable>?) {
        val parcelList: List<IKmmParcelable>? = value?.mapNotNull { it.checkDeepCopy() }
        parcelDataQueue.enqueue(parcelList)
    }

    override fun <T : IKmmParcelable> readParcelList(clazz: KClass<T>, dest: () -> T): List<T>? {
        val parcelList: List<*>? = parcelDataQueue.dequeue() as? List<*>
        return parcelList?.mapNotNull { clazz.safeCast(it) }
    }

    private fun IKmmParcelable?.checkDeepCopy(): IKmmParcelable? {
        return if (enableDeepCopy && this is IKmmParcelCloneable) {
            this.kmmParcelClone()
        } else {
            this
        }
    }

    override fun <T> writeList(value: List<T>?) {
        // 【注意】深拷贝一个 List
        parcelDataQueue.enqueue(ArrayList(value ?: emptyList()))
    }

    // 【注意】这个 safeCast List 不绝对安全，只能大致匹配到是个 List，里面的泛型会被抹除
    override fun <T : Any> readList(clazz: KClass<T>): List<T>? =
        parcelDataQueue.dequeue().safeCast<List<T>>()

    override fun <K, V> writeMap(value: Map<K, V>?) {
        // 【注意】深拷贝一个 Map
        parcelDataQueue.enqueue(HashMap(value ?: emptyMap()))
    }

    // 【注意】这个 safeCast Map 不绝对安全，只能大致匹配到是个 Map，里面的泛型会被抹除
    override fun <K, V> readMap(): Map<K, V>? = parcelDataQueue.dequeue().safeCast<Map<K, V>>()

    private inline fun <reified T> Any?.safeCast(): T? {
        val value = this ?: return null
        return if (value is T) {
            value
        } else {
            if (isDebug()) {
                throw RuntimeException("DeepCopyKmmParcel 读取 ${T::class.simpleName}=${value} 时发生类型异常，请检查读写时序！")
            }
            null
        }
    }

    companion object {
        fun <T : IKmmParcelable> parcelClone(from: T, to: T): T {
            val cloneParcel = DeepCopyKmmParcel()
            from.writeToKmmParcel(cloneParcel)

            to.readFromKmmParcel(cloneParcel)

            (to as? IDtoHolderItem)?.resetAllDto()
            (to as? IVMHolderItem)?.resetAllVM()

            return to
        }
    }
}