package com.tencent.news.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 解析Double类型，服务端如果下发乱七八糟的字符，不会导致crash，返回默认值0.0
 */
object SafeDoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Safe.Double", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): Double {
        runCatching {
            val string = decoder.decodeString()
            return string.toDouble()
        }.getOrElse {
            return 0.0
        }
    }

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }
}