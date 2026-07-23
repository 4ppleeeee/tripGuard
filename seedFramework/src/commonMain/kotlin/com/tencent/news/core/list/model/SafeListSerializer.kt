package com.tencent.news.core.list.model

import com.tencent.news.core.extension.getNonNullArray
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement


@Suppress("AnnotationOnSeparateLine")
typealias SafeStringList = @Serializable(SafeStringListSerializer::class) List<String>

// 字符串数组解析，会过滤掉数组中的null
object SafeStringListSerializer : SafeListSerializer<String>(
    serializer = String.serializer()
)

// 数组解析
open class SafeListSerializer<T>(private val serializer: KSerializer<T>) : KSerializer<List<T>> {
    override val descriptor: SerialDescriptor = ListSerializer(serializer).descriptor

    override fun serialize(encoder: Encoder, value: List<T>) {
        encoder.encodeSerializableValue(ListSerializer(serializer), value)
    }

    override fun deserialize(decoder: Decoder): List<T> {
        val originJsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        return when (originJsonElement) {
            is JsonArray ->
                KtJson.safeDecode(ListSerializer(serializer), originJsonElement.getNonNullArray())
                    ?: listOf()

            else -> listOf() // 如果不是 JsonArray，返回空列表
        }
    }

}

// MutableList 版本的安全列表序列化器（过滤数组中的null）
open class SafeMutableListSerializer<T>(private val serializer: KSerializer<T>) :
    KSerializer<MutableList<T>> {
    override val descriptor: SerialDescriptor = ListSerializer(serializer).descriptor

    override fun serialize(encoder: Encoder, value: MutableList<T>) {
        encoder.encodeSerializableValue(ListSerializer(serializer), value)
    }

    override fun deserialize(decoder: Decoder): MutableList<T> {
        val originJsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        return when (originJsonElement) {
            is JsonArray ->
                KtJson.safeDecode(ListSerializer(serializer), originJsonElement.getNonNullArray())
                    ?.toMutableList() ?: mutableListOf()

            else -> mutableListOf()
        }
    }
}