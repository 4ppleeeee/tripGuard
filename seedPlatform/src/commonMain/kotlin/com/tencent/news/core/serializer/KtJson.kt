package com.tencent.news.core.serializer

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.platform.PlatformJsonLog
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder


// kotlinx.serialization 库用法：https://juejin.cn/post/6973874488345624584
// 官方git：https://github.com/Kotlin/kotlinx.serialization
// api文档：https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md
val KtJson = createBaseJson(null)

val KtTrimJson = createBaseJson {
    encodeDefaults = false // 精简json尺寸，与默认值相同的字段，不输出
}

/**
 * 可以兼容的情况：
 *
 * 定义：var id: String = "123"
 * - 下发 id = null，可以兼容，且解析为默认值 "123"
 *
 * 定义：var id: String = "123"
 * - 下发 id = 456，可以兼容，且解析为 "456"
 *
 * 定义：var id: Int = 123
 * - 下发 id = "456"，可以兼容，且解析为 456
 *
 * 定义：var id: Int = 123
 * - 下发 id = "test"，解析会报错
 *
 * 定义：var list: List<Item>? = null
 * - 下发 [ null, {}, {}]，null的解析会报错，需要自己兼容，见：QnCompatSerializer
 */
// 基础 json 解析配置（特殊业务场景可以派生自己的实例进行扩展）
fun createBaseJson(builder: (JsonBuilder.() -> Unit)?): Json {
    return Json {

        ignoreUnknownKeys = true    // 解析到不认识的key，跳过；不报错

        isLenient = true            // 宽松模式：反序列化过程中尽可能多地解析 JSON 数据

        encodeDefaults = true       // 当属性值与默认值相等时，是否要输出到json里（false 的话能节约json尺寸）

        coerceInputValues = true    // 支持默认值（字段没下发 或 下发null的时候，使用默认值）

        builder?.invoke(this)
    }
}

@Deprecated("不规范用法", ReplaceWith("JsonEx 相关扩展方法"))
object KtJsonUtil {

    @Deprecated("不规范用法", ReplaceWith("KtJson.safeEncode"))
    inline fun <reified T> toJsonStr(obj: T?): String {
        obj ?: return ""
        runCatching {
            return KtJson.safeEncode(obj)
        }.getOrElse { error ->
            PlatformJsonLog.error("", "toJsonStr failed", error)
        }
        return ""
    }

    @Deprecated("不规范用法", ReplaceWith("KtJson.safeDecode"))
    inline fun <reified T> fromJson(json: String?): T? {
        return KtJson.safeDecode(json)
    }

}
