package com.tencent.news.core.list.model

import com.tencent.news.core.extension.dispatchAfterJsonParse
import com.tencent.news.core.extension.getCurTimestampMillis
import com.tencent.news.core.extension.getJsonStr
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.toJsonObject
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.platform.IQnKmmModelParser
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.LazyImpl
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject


// todo 注意：新增兼容性接口后，要在宿主注册解析 StructWidgetGsonKt.createWidgetGsonBuilder

var debugParserTimeCost = false // 需要调试的时候再打开

open class QnCompatSerializer<T>(
    private val qnParser: LazyImpl<IQnKmmModelParser<T>?> = { null },
    val kmmSerializer: LazyImpl<KSerializer<out T>>,
    private val enableBaseItemParser: Boolean = false,
) : OriginJsonSerializer<T>(kmmSerializer) {

    private val realParser get() = qnParser()

    override fun deserialize(decoder: Decoder): T {
        // 鸿蒙直接用原始数据
        if (isHarmonyPlatform()) {
//            return decoder.decodeSerializableValue(kmmSerializer)
        }

        if (realParser == null) {
            return super.deserialize(decoder)
        }

        // 优先用JsonElement直接解析，节约耗时
        var startTime = getCurTimestampMillis()
        val originJsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
        val qnJsonElementResult = realParser?.decodeFromJson(originJsonElement)
        if (qnJsonElementResult != null) {
            debugLog { "JsonElement方式解析耗时：${getCurTimestampMillis() - startTime}" }
            qnJsonElementResult.dispatchAfterJsonParse()
            return qnJsonElementResult
        }

        val originJsonStr = originJsonElement.getJsonStr()
        val jsonStrCost = getCurTimestampMillis() - startTime
        startTime = getCurTimestampMillis()

        // 遇到 null 用空对象兜底
        if (originJsonStr.isBlank()) {
            return decodeEmpty()
        }

        val baseItem = if (isIOSPlatform() && enableBaseItemParser) {
            KtJson.safeDecode(kmmSerializer(), originJsonStr)
        } else {
            null
        }

        val result = realParser?.decodeFromJson(originJsonStr, baseItem)
            ?: KtJson.safeDecode(kmmSerializer(), originJsonStr)
            ?: decodeEmpty()

        val jsonStrResultCost = getCurTimestampMillis() - startTime
        debugLog { "JsonStr方式解析耗时：strCost=${jsonStrCost}, resultCost=${jsonStrResultCost}" }

        result.dispatchAfterJsonParse(originJsonStr)
        return result
    }

    private inline fun debugLog(msg: () -> String) {
        if (!isDebug()) {
            return
        }
        if (!debugParserTimeCost) {
            return
        }
        NewsFeedsSLO.debugLog { "${getLogKey()} ${msg()}" }
    }

    @Suppress("OPT_IN_USAGE")
    private fun getLogKey(): String = kmmSerializer().descriptor.serialName

    override fun serialize(encoder: Encoder, value: T) {
        if (realParser == null) {
            super.serialize(encoder, value)
            return
        }

        val finalJson = realParser?.encodeToJson(value)
        val jsonObj = finalJson.toJsonObject() ?: JsonObject(emptyMap())
        return encoder.encodeSerializableValue(JsonObject.serializer(), jsonObj)
    }

    fun createQnInstance(default: () -> T): T = realParser?.decodeFromJson("{}") ?: default()

}