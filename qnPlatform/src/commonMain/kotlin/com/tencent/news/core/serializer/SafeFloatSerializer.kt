package com.tencent.news.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 解析Float类型，服务端如果下发乱七八糟的字符，不会导致crash，返回默认值0f
 */
object SafeFloatSerializer : KSerializer<Float> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Safe.Float", PrimitiveKind.FLOAT)

    override fun deserialize(decoder: Decoder): Float {
        runCatching {
            val string = decoder.decodeString()
            return string.toFloat()
        }.getOrElse {
            return 0f
        }
    }

    override fun serialize(encoder: Encoder, value: Float) {
        encoder.encodeFloat(value)
    }
}