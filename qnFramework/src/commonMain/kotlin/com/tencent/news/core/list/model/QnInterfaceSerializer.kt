package com.tencent.news.core.list.model

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.unSafeDecode
import com.tencent.news.core.parcel.DeepCopyKmmParcel
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

// 用于将抽象接口，解析为具体实现类，常用于：
// 完全下沉到kmm中的model类，想将class internal化隐藏起来，进行数据解析的依赖注入时使用
open class QnInterfaceSerializer<T : Any>(private val clazz: KClass<T>) : OriginJsonSerializer<T>({
    GlobalModelSerializerFactory.getDefaultSerializer(clazz)
})

interface IQnInterfaceCreator<T> {
    fun defaultSerializer(): KSerializer<out T>
}

// new一个空实例，理论上解析不可能失败
inline fun <reified T> IQnInterfaceCreator<T>.new(json: String = "{}"): T =
    KtJson.unSafeDecode(defaultSerializer(), json)

inline fun <reified T> IQnInterfaceCreator<T>.new(initAction: T.() -> Unit): T =
    new().apply(initAction)

inline fun <reified T> IQnInterfaceCreator<T>.safeDecode(json: String = "{}"): T? =
    KtJson.safeDecode(defaultSerializer(), json)

inline fun <reified T : IKmmParcelable> IKmmParcel.readParcelable(creator: IQnInterfaceCreator<T>): T? =
    readParcelable(T::class) { creator.new() }

inline fun <reified T : IKmmParcelable> IKmmParcel.readParcelList(creator: IQnInterfaceCreator<T>): List<T>? =
    readParcelList(T::class) { creator.new() }

inline fun <reified T : IKmmParcelable> IQnInterfaceCreator<T>.parcelCloneFrom(from: T): T =
    DeepCopyKmmParcel.parcelClone(from, new())