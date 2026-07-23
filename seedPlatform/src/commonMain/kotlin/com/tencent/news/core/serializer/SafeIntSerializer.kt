package com.tencent.news.core.serializer

import com.tencent.news.core.extension.safeToInt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Suppress("AnnotationOnSeparateLine")
typealias SafeInt = @Serializable(SafeIntSerializer::class) Int

/**
 * 解析Int类型，服务端如果下发乱七八糟的字符，不会导致crash，返回默认值0
 */
object SafeIntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = Int.serializer().descriptor

    override fun deserialize(decoder: Decoder): Int {
        runCatching {
            val string = decoder.decodeString()
            return string.safeToInt()
        }.getOrElse {
            return 0
        }
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }

}