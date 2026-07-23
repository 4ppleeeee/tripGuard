package com.tencent.news.core.serializer

import com.tencent.news.core.extension.safeToLong
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Suppress("AnnotationOnSeparateLine")
typealias SafeLong = @Serializable(SafeLongSerializer::class) Long

/**
 * 解析Long类型，服务端如果下发乱七八糟的字符，不会导致crash，返回默认值0L
 */
object SafeLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = Long.serializer().descriptor

    override fun deserialize(decoder: Decoder): Long {
        runCatching {
            val string = decoder.decodeString()
            return string.safeToLong()
        }.getOrElse {
            return 0L
        }
    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }
}