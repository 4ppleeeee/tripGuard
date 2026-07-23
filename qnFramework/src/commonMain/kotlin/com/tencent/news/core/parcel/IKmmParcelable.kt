package com.tencent.news.core.parcel

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.platform.api.isDebug
import kotlin.reflect.KClass


// 可以同时用于：
// 1. 安卓的parcel序列化传输
// 2. 借用parcel机制，实现iOS的手动copy操作 @DeepCopyKmmParcel
interface IKmmParcelable {
    // todo 【注意】读写字段顺序要保持一致
    fun writeToKmmParcel(dest: IKmmParcel)
    fun readFromKmmParcel(from: IKmmParcel)
}

interface IKmmParcelableCreator<T : IKmmParcelable> {
    fun getKmmParcelClass(): KClass<T>
    fun createParcelObject(): T
}

interface IKmmDtoParcelable : IKmmParcelable {

    fun getParcelDtoList(): List<IKmmParcelable?>

    override fun writeToKmmParcel(dest: IKmmParcel) {
        getParcelDtoList().forEach {
            it?.writeToKmmParcel(dest)
        }
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        getParcelDtoList().forEach {
            it?.readFromKmmParcel(from)
        }
    }
}

interface IKmmParcel {

    fun writeInt(value: Int)
    fun readInt(): Int

    fun writeLong(value: Long)
    fun readLong(): Long

    fun writeFloat(value: Float)
    fun readFloat(): Float

    fun writeDouble(value: Double)
    fun readDouble(): Double

    fun writeString(value: String?)
    fun readString(): String

    fun writeBoolean(value: Boolean)
    fun readBoolean(): Boolean

    fun writeSerializable(value: IKmmKeep?)
    fun readSerializable(): IKmmKeep?

    fun writeParcelable(value: IKmmParcelable?)
    fun <T : IKmmParcelable> readParcelable(clazz: KClass<T>, dest: () -> T): T?

    fun writeParcelList(value: List<IKmmParcelable>?)
    fun <T : IKmmParcelable> readParcelList(clazz: KClass<T>, dest: () -> T): List<T>?

    fun <T> writeList(value: List<T>?)
    fun <T : Any> readList(clazz: KClass<T>): List<T>?

    fun <K, V> writeMap(value: Map<K, V>?)
    fun <K, V> readMap(): Map<K, V>?

}

inline fun <reified T : IKmmKeep> IKmmParcel.safeRead(action: (IKmmParcel) -> Any?): T? {
    val value = action(this) ?: return null
    return if (value is T) {
        value
    } else {
        if (isDebug()) {
            throw RuntimeException("safeRead ${T::class.simpleName}=${value} 时发生类型异常，请检查读写时序！")
        }
        null
    }
}

inline fun <reified T : Any> IKmmParcel.safeReadList(clazz: KClass<T>): List<T> =
    readList(clazz) ?: listOf()

// readParcelable 容易重名，换成这个
inline fun <reified T : IKmmParcelable> IKmmParcel.safeReadParcel(creator: IKmmParcelableCreator<T>): T? =
    readParcelable(creator)

inline fun <reified T : IKmmParcelable> IKmmParcel.readParcelable(creator: IKmmParcelableCreator<T>): T? {
    return readParcelable(creator.getKmmParcelClass()) {
        creator.createParcelObject()
    }
}

inline fun <reified T : IKmmParcelable> IKmmParcel.safeReadParcelList(creator: IKmmParcelableCreator<T>): List<T>? {
    return readParcelList(creator.getKmmParcelClass()) {
        creator.createParcelObject()
    }
}