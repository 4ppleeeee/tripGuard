@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getJsonStr
import com.tencent.news.core.tads.api.IAdHolder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement


@Serializable
@SerialName(StructWidgetType.AD_LIST)
class AdListWidget : StructWidget(), IKmmKeep {

    @Serializable(AdListWidgetDataWrapperSerializer::class)
    var data: AdListWidgetData? = null

    // 迁移了kmm逻辑才有这个
    @Transient
    @kotlin.jvm.Transient
    var adHolder: IAdHolder? = null

    override fun getWidgetType() = StructWidgetType.AD_LIST

}

class AdListWidgetDataWrapperSerializer : DataWrapperSerializer<AdListWidgetData>(
    StructWidgetType.AD_LIST, AdListWidgetData.serializer()
)

@Serializable
class AdListWidgetData : StructWidgetData(), IKmmKeep {

    @Suppress("SerialNameAtPublicClass")
    @SerialName("ad_info")
    @Serializable(AdListCompatSerializer::class)
    var adListJson: String? = null

}

// 兼容 string 和 obj 两种格式解析
object AdListCompatSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): String {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        return jsonElement.getJsonStr()
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}