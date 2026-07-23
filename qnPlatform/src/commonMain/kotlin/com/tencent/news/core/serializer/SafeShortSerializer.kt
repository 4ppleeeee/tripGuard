package com.tencent.news.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 解析Short类型，服务端如果下发乱七八糟的字符，不会导致crash，返回默认值0
 */
object SafeShortSerializer : KSerializer<Short> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Safe.Short", PrimitiveKind.SHORT)

    override fun deserialize(decoder: Decoder): Short {
        runCatching {
            val string = decoder.decodeString()
            return string.toShort()
        }.getOrElse {
            return 0
        }
    }

    override fun serialize(encoder: Encoder, value: Short) {
        encoder.encodeShort(value)
    }
}