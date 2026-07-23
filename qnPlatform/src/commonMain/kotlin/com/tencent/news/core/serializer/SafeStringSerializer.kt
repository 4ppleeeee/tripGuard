package com.tencent.news.core.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 解析String类型，服务端如果下发乱七八糟的字符，不会导致crash，返回默认值""
 */
object SafeStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Safe.String", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        runCatching {
            return decoder.decodeString()
        }.getOrElse {
            return ""
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}