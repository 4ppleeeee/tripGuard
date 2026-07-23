package com.tencent.news.core.list.model

import com.tencent.news.core.extension.getJsonStr
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.unSafeDecode
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.LazyImpl
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject


// 可以同时兼容 json string 或 obj 格式的解析
open class JsonToObjSerializer<T>(
    val serializer: LazyImpl<KSerializer<out T>>,
    val emptyJson: String = "{}",
) : KSerializer<T> {
    override val descriptor = JsonObject.serializer().descriptor

    override fun deserialize(decoder: Decoder): T {
        val originJsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        val originJsonStr = originJsonElement.getJsonStr()

        // 遇到 null 用空对象兜底
        if (originJsonStr.isBlank()) {
            return decodeEmpty()
        }

        return KtJson.safeDecode(serializer(), originJsonStr) ?: decodeEmpty()
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeSerializableValue(serializer() as KSerializer<T>, value)
    }

    fun decodeEmpty(): T = KtJson.unSafeDecode(serializer(), emptyJson)

}

// 保留原始json的解析器
open class OriginJsonSerializer<T>(
    serializer: LazyImpl<KSerializer<out T>>,
) : JsonToObjSerializer<T>(serializer)

// 下发jsonObj也转换成 json String 格式
class JsonStrSerializer : KSerializer<String> {
    override val descriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): String =
        decoder.decodeSerializableValue(JsonElement.serializer()).getJsonStr()

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

// 可以同时兼容 json array string 或 List<obj> 格式的解析
open class JsonToObjListSerializer<T>(serializer: LazyImpl<KSerializer<T>>) :
    JsonToObjSerializer<List<T>>(
        serializer = { ListSerializer(serializer()) },
        emptyJson = "[]"
    )