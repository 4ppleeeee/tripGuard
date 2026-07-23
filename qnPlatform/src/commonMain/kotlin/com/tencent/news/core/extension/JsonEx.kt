package com.tencent.news.core.extension

import com.tencent.news.core.list.model.IAfterParseCompat
import com.tencent.news.core.list.model.IOriginJson
import com.tencent.news.core.list.model.IOriginJsonMap
import com.tencent.news.core.list.trace.NewsJson
import com.tencent.news.core.platform.api.appReport
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.platform.api.runDebug
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.serializer.KtTrimJson
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.serializer

// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash；
//  这个方法用于兼容查找 QnXXX 方式注册的接口
inline fun <reified T> Json.compatGetDeserializer(): DeserializationStrategy<T> {
    return runCatching {
        // 如果T是类似 typealias QnXXX = @Serializable(IXXX::class)，KN上无法找到对应的serializer。
        serializersModule.serializer<T>()
    }.getOrElse {
        // 一般会用接口直接解析的，都是 QnXXX 注解的，此处兜底从工厂查询一次
        // 如果业务侧真的就用个普通接口调用，这里还是会crash的
        GlobalModelSerializerFactory.getDefault()
    }
}

// kt所有json解析都过到这里，可以统一处理拦截
// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash
inline fun <reified T> Json.safeDecode(json: String?, reportError: Boolean = true): T? =
    safeDecode(compatGetDeserializer(), json, reportError)

// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash
fun <T> Json.safeDecode(
    serializer: DeserializationStrategy<T>,
    json: String?,
    reportError: Boolean = true,
): T? {
    if (json.isNullOrEmpty()) {
//        NewsJson.warn { "${serializer.getLogMsg()} json为空，解析失败" }
        return null
    }

    runCatching {
        return unSafeDecode(serializer, json)
    }.getOrElse { error ->
        if (reportError) {
            error.throwDebugException()
        }
        return null
    }
}

// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash
@Throws(RuntimeException::class)
inline fun <reified T> Json.unSafeDecode(json: String?): T =
    unSafeDecode(compatGetDeserializer(), json)

@Throws(RuntimeException::class)
fun <T> Json.unSafeDecode(serializer: DeserializationStrategy<T>, json: String?): T {
    runCatching {
        val result: T = decodeFromString(serializer, json.getNonNull())
        result.dispatchAfterJsonParse(json)
        return result
    }.getOrElse { error ->
        // error 信息单独打印，否则淹没在json里不好看
        NewsJson.error("${serializer.getLogMsg()} safeDecode 解析失败", error)
        NewsJson.error("${serializer.getLogMsg()} safeDecode 异常json：${json}")
        throw error
    }
}

/**
 * 安全解析JsonElement为指定类型T
 */
// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash
inline fun <reified T> Json.safeDecode(jsonElement: JsonElement): T? {
    runCatching {
        return unSafeDecode(jsonElement)
    }.getOrElse { error ->
        NewsJson.error("safeDecode JsonElement 解析失败：${jsonElement}", error)
        error.throwDebugException()
        return null
    }
}

fun <T> Json.safeDecode(serializer: DeserializationStrategy<T>, jsonElement: JsonElement): T? {
    runCatching {
        return unSafeDecode(serializer, jsonElement)
    }.getOrElse { error ->
        NewsJson.error("safeDecode JsonElement 解析失败：${jsonElement}", error)
        error.throwDebugException()
        return null
    }
}

/**
 * 不安全解析JsonElement为指定类型T，解析失败会抛出异常
 */
// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash
@Throws(RuntimeException::class)
inline fun <reified T> Json.unSafeDecode(jsonElement: JsonElement): T =
    unSafeDecode(compatGetDeserializer(), jsonElement)

/**
 * 使用指定序列化器不安全解析JsonElement，解析失败会抛出异常
 */
@Throws(RuntimeException::class)
fun <T> Json.unSafeDecode(serializer: DeserializationStrategy<T>, jsonElement: JsonElement): T {
    val result: T = decodeFromJsonElement(serializer, jsonElement)
    result.dispatchAfterJsonParse()
    return result
}

inline fun <reified T> Json.safeEncode(obj: T?): String = safeEncode(serializer(), obj)

/**
 * 根据对象的[kotlin.reflect.KClass]序列化成Json，如果当前对象未标记[kotlinx.serialization.Serializable]，则返回`{}`
 */
@OptIn(InternalSerializationApi::class)
fun <T : Any> Json.safeEncodeByKClass(obj: T): String {
    try {
        val serializer = obj::class.serializer() as KSerializer<T>
        return safeEncode(serializer, obj)
    } catch (e: Exception) {
        return "{}"
    }
}

fun <T> Json.safeEncode(serializer: SerializationStrategy<T>, obj: T?): String {
    runCatching {
        return unSafeEncode(serializer, obj)
    }.getOrElse { error ->
        NewsJson.error("$obj safeEncode failed", error)
        error.throwDebugException()
        return ""
    }
}

// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash
@Throws(RuntimeException::class)
inline fun <reified T> Json.unSafeEncode(obj: T?): String =
    unSafeEncode(serializersModule.serializer(), obj)

@Throws(RuntimeException::class)
fun <T> Json.unSafeEncode(serializer: SerializationStrategy<T>, obj: T?): String {
    obj ?: return ""
    return encodeToString(serializer, obj)
}

fun <T> Json.safeEncodeJsonObj(serializer: SerializationStrategy<T>, obj: T?): JsonObject? {
    obj ?: return null

    runCatching {
        return encodeToJsonElement(serializer, obj) as? JsonObject
    }.getOrElse { error ->
        NewsJson.error("$obj safeEncode failed", error)
        error.throwDebugException()
        return null
    }
}

// todo 【警告】泛型不能使用接口类型，在iOS和鸿蒙会crash
inline fun <reified T> Json.safeEncodeJsonObj(obj: T?): JsonObject? =
    safeEncodeJsonObj(serializersModule.serializer(), obj)

fun Json.safeDecodeJsonObj(json: String?): JsonObject? = safeDecode<JsonObject>(json)

fun String?.safeDecodeJsonObj(): JsonObject? = KtJson.safeDecodeJsonObj(this)

fun Json.safeDecodeStringMap(json: String?): Map<String, String>? =
    safeDecode<Map<String, String>>(json)

inline fun <reified T> Json.safeEncodeBasicMap(obj: T?): PrimitiveMap? =
    safeEncodeJsonObj(obj)?.jsonObj2Map()

fun Any?.dispatchAfterJsonParse(originJsonStr: String? = "") {
    val result = this ?: return

    // 保留原始json串
    if (!originJsonStr.isNullOrEmpty()) {
        (result as? IOriginJson)?.originJson = originJsonStr

        if (result is IOriginJsonMap) {
            val jsonMap = originJsonStr.toJsonObject()?.jsonObj2Map()
            if (jsonMap != null) {
                result.originJsonMap = jsonMap
            }
        }
    }

    // 解析完成后，可以执行一些数据兼容逻辑
    (result as? IAfterParseCompat)?.onCompatDataAfterParse()
    if (result is List<*>) {
        result.forEach {
            (it as? IAfterParseCompat)?.onCompatDataAfterParse()
        }
    }
}

// todo 【注意】这个方法，泛型只能传递 实现类，不能用接口（接口获取 serializer() 会解析失败）
//  如果泛型是 接口 类型，用下面带 serializer 参数的
inline fun <reified T> T.safeJsonClone(): T? = safeJsonClone(serializer())

inline fun <reified T> T.safeJsonClone(serializer: KSerializer<T>): T? {
    val originJson = if (this is IOriginJson) originJson else ""

    val cloneJson = KtTrimJson.safeEncode(serializer, this)
    val result = KtTrimJson.safeDecode(serializer, cloneJson)

    if (result is IOriginJson) {
        result.originJson = originJson
    }
    return result
}

/**
 * 防止返回"null"，默认值""
 */
fun jsonValuedString(str: String?): JsonPrimitive {
    if (str.isNullOrEmpty()) {
        return JsonPrimitive("")
    }
    return JsonPrimitive(str)
}

/**
 * 防止返回"null"，默认值false
 */
fun jsonValuedBoolean(boolean: Boolean?): JsonPrimitive {
    if (boolean == null) {
        return JsonPrimitive(false)
    }
    return JsonPrimitive(boolean)
}

/**
 * 防止返回"null"，默认值0
 */
fun jsonValuedNum(value: Number?): JsonPrimitive {
    if (value == null) {
        return JsonPrimitive(0)
    }
    // 如果是浮点数且小数部分为0，转成整数类型
    // 例如: 1.0 -> 1, 2.0 -> 2
    // 用于解决kt对不认识的number类型，会转成double类型，导致json解析失败
    if (value is Double && value.toDouble() % 1 == 0.0) {
        return JsonPrimitive(value.toLong())
    }

    return JsonPrimitive(value)
}

/**
 * Map -> JsonElements
 */
fun Map<String, Any?>.convertToJsonElements(): JsonObject {
    return JsonObject(
        mapValues { (_, value) ->
            when (value) {
                null -> JsonNull
                is Map<*, *> -> value.mapKeys { entry -> entry.key.toString() }
                    .convertToJsonElements()

                is List<*> -> value.convertToJsonElements()
                else -> value.convertPrimitiveJsonElement() ?: JsonNull
            }
        }
    )
}

/**
 * List -> JsonElements
 */
fun List<*>.convertToJsonElements(): JsonArray {
    return JsonArray(
        mapNotNull { item ->
            when (item) {
                null -> null
                is Map<*, *> -> item.mapKeys { entry -> entry.key.toString() }
                    .convertToJsonElements()

                is List<*> -> item.convertToJsonElements()
                else -> item.convertPrimitiveJsonElement() ?: JsonNull
            }
        }
    )
}

/**
 * String -> JsonArray, maybe null
 */
fun String?.toJsonArray(): JsonArray? {
    if (isNullOrEmpty()) {
        return null
    }
    runCatching {
        return Json.parseToJsonElement(this) as JsonArray
    }.getOrElse { error ->
        NewsJson.error("str is not json array : $this", error)
        return null
    }
}

/**
 * String -> JsonObject, maybe null
 */
fun String?.toJsonObject(): JsonObject? {
    if (isNullOrEmpty()) {
        return null
    }
    runCatching {
        return Json.parseToJsonElement(this) as? JsonObject
    }.getOrElse { error ->
        NewsJson.error("str is not json object : $this", error)
        return null
    }
}

fun JsonObject?.optJsonObject(key: String): JsonObject? =
    this?.get(key)?.jsonObjectOrNull

fun JsonObject?.optJsonArray(key: String): JsonArray? =
    this?.get(key)?.jsonArrayOrNull

fun JsonObject?.optString(key: String): String? =
    this?.get(key)?.jsonPrimitiveOrNull?.content

fun JsonObject?.optBoolean(key: String): Boolean =
    this?.get(key)?.jsonPrimitiveOrNull?.booleanOrNull ?: false

fun JsonObject?.optInt(key: String): Int =
    this?.get(key)?.jsonPrimitiveOrNull?.intOrNull ?: 0

fun JsonObject?.optLong(key: String): Long =
    this?.get(key)?.jsonPrimitiveOrNull?.longOrNull ?: 0L

fun JsonObject?.optFloat(key: String): Float =
    this?.get(key)?.jsonPrimitiveOrNull?.floatOrNull ?: 0.0f

fun JsonObject?.optDouble(key: String): Double =
    this?.get(key)?.jsonPrimitiveOrNull?.doubleOrNull ?: 0.0

fun JsonArray?.optJsonArray(index: Int): JsonArray? =
    this?.safeGet(index)?.jsonArrayOrNull

fun JsonArray?.optJsonObject(index: Int): JsonObject? =
    this?.safeGet(index)?.jsonObjectOrNull

fun JsonArray?.optString(index: Int): String? =
    this?.safeGet(index)?.jsonPrimitiveOrNull?.content

fun JsonArray?.optBoolean(index: Int): Boolean =
    this?.safeGet(index)?.jsonPrimitiveOrNull?.booleanOrNull ?: false

fun JsonArray?.optInt(index: Int): Int =
    this?.safeGet(index)?.jsonPrimitiveOrNull?.intOrNull ?: 0

fun JsonArray?.optLong(index: Int): Long =
    this?.safeGet(index)?.jsonPrimitiveOrNull?.longOrNull ?: 0L

fun JsonArray?.optFloat(index: Int): Float =
    this?.safeGet(index)?.jsonPrimitiveOrNull?.floatOrNull ?: 0.0f

fun JsonArray?.optDouble(index: Int): Double =
    this?.safeGet(index)?.jsonPrimitiveOrNull?.doubleOrNull ?: 0.0

private val JsonElement.jsonObjectOrNull: JsonObject? get() = this as? JsonObject

private val JsonElement.jsonArrayOrNull: JsonArray? get() = this as? JsonArray

private val JsonElement.jsonPrimitiveOrNull: JsonPrimitive? get() = this as? JsonPrimitive

fun JsonObject?.getString(key: String): String? = this?.get(key)?.jsonPrimitiveOrNull?.content

fun JsonElement?.getJsonStr(): String {
    this ?: return ""

    if (this is JsonNull) {
        return ""
    }
    if (this is JsonPrimitive) {
        return this.content // 注意，这里 toString 和 content 在转义上会有区别（content 会去除转义）
    }
    return this.toString()
}

fun JsonPrimitive?.isBoolean(): Boolean = this?.booleanOrNull != null

fun JsonPrimitive?.isNumber(): Boolean {
    this ?: return false

    return intOrNull != null ||
            longOrNull != null ||
            floatOrNull != null ||
            doubleOrNull != null
}

fun JsonPrimitive?.getAsString(): String = this?.content ?: ""
fun JsonPrimitive?.getAsBoolean(): Boolean = this?.booleanOrNull ?: false
fun JsonPrimitive?.getAsDouble(): Double = this?.doubleOrNull ?: 0.0
fun JsonPrimitive?.getAsLong(): Long = this?.longOrNull ?: 0
fun JsonPrimitive?.getAsInt(): Int = this?.intOrNull ?: 0


fun Any.convertPrimitiveJsonElement(): JsonElement? {
    return when (this) {
        is JsonElement -> this
        is Number -> jsonValuedNum(this)
        is String -> jsonValuedString(this)
        is Boolean -> jsonValuedBoolean(this)
        else -> null
    }
}

fun Map<String, Any?>?.toJson(): String =
    this?.convertToJsonElements().safeEncodeToJson()

fun Map<String, *>?.safeEncodeToJson(): String {
    this ?: return "{}"
    return KtJson.safeEncode(convertToJsonElementMap())
}

fun Map<String, *>.convertToJsonElementMap(): Map<String, JsonElement> {
    val result = mutableMapOf<String, JsonElement>()

    this.forEach {
        val key = it.key
        val value = it.value
            ?: return@forEach

        val primitive = value.convertPrimitiveJsonElement()
        if (primitive != null) {
            result[key] = primitive
        } else {
            result[key] = when (value) {
                is List<*> -> value.convertToJsonElements()

                is Map<*, *> -> JsonObject(
                    value.mapKeys { entry -> entry.key.toString() }.convertToJsonElementMap()
                )

                else -> if (isDebug()) {
                    throw IllegalArgumentException("$key = $value (${value::class.simpleName}) 转换成 Map 失败，请兼容！")
                } else {
                    jsonValuedString(value.toString())
                }
            }
        }
    }

    return result
}

inline fun <reified T> T?.toJsonElement(): JsonElement? {
    this ?: return null
    runCatching {
        return KtJson.encodeToJsonElement<T>(this)
    }.getOrElse { error ->
        NewsJson.error("$this json解析失败", error)
        error.throwDebugException()
        return null
    }
}


fun JsonObject.jsonObj2Map(): PrimitiveMap {
    val result = mutableMapOf<String, Any>()
    forEach {
        val key = it.key
        if (it.value is JsonNull) {
            return@forEach
        }

        when (val value = it.value) {
            is JsonObject -> result[key] = value.jsonObj2Map()
            is JsonPrimitive -> result[key] = value.jsonPrimitiveToValue()
            is JsonArray -> result[key] = value.jsonArrayToList()
            else -> runDebug { throw IllegalArgumentException("$value 转换成 Map 失败，请兼容！") }
        }
    }
    return result
}

fun JsonPrimitive.jsonPrimitiveToValue(): Any {
    if (isString) {
        return content
    }
    intOrNull?.let { return it }
    longOrNull?.let { return it }
    floatOrNull?.let { return it }
    doubleOrNull?.let { return it }
    booleanOrNull?.let { return it }
    return content
}

fun JsonArray.jsonArrayToList(): List<Any> {
    val list = mutableListOf<Any>()
    val jsonArray = jsonArray
    jsonArray.forEach {
        when (it) {
            is JsonArray -> list.add(it.jsonArrayToList())
            is JsonPrimitive -> list.add(it.jsonPrimitiveToValue())
            is JsonObject -> list.add(it.jsonObj2Map())
        }
    }
    return list
}

fun JsonArray.filterNotNullArray(): JsonArray {
    val originJson = this
    return buildJsonArray {
        originJson.filterNot { it is JsonNull }
            .forEach { add(it) }
    }
}

fun JsonArray.getNonNullArray(): JsonArray {
    return if (this.contains(JsonNull)) {
        this.filterNotNullArray()
    } else {
        this
    }
}

// kmm 数据结构直接作为网络库请求参数时，需要使用本方法进行转换，转成基础数据结构的map用于json请求
// （如果直接传数据结构，宿主侧无法识别 @SerialName 注解，请求参数名不对）
inline fun <reified T : IKmmKeep> T?.toPrimitiveJsonMap(): PrimitiveMap? {
    this ?: return null
    runCatching {
        return KtJson.safeEncode(this).toJsonObject()?.jsonObj2Map()
    }.getOrElse { error ->
        NewsJson.error("$this 转换失败", error)
        error.throwDebugException()
        return null
    }
}

fun String?.toPrimitiveJsonMap(): PrimitiveMap? = this?.toJsonObject()?.jsonObj2Map()

fun DeserializationStrategy<*>.getLogMsg(): String = descriptor.toString()

private val reportJsonBugly by lazy { getShiplySwitch("report_kmm_json_bugly", true) }

fun Throwable.throwDebugException() {
    if (reportJsonBugly) {
        appReport().reportBugly("kotlin json解析失败", this)
    }
    debugToast("【警告⚠️】json解析失败，请查看 NewsJson 日志")
}

class JsonObjectModifier internal constructor(map: Map<String, JsonElement>) {

    private val content: MutableMap<String, JsonElement> =
        linkedMapOf<String, JsonElement>().apply {
            putAll(map)
        }

    fun put(key: String, element: JsonElement): JsonElement? = content.put(key, element)
    fun remove(key: String) = content.remove(key)
    internal fun build(): JsonObject = JsonObject(content)
}

fun JsonElement.copyNew(
    builderAction: JsonObjectModifier.() -> Unit
): JsonElement {
    if (this !is JsonObject) {
        if (isDebug()) {
            throw IllegalArgumentException("JsonElement 不是JsonObject，请兼容！")
        }
        return this
    }
    val builder = JsonObjectModifier(this.jsonObject)
    builder.builderAction()
    return builder.build()
}

fun String?.simpleCheckJsonValid(): Boolean {
    this ?: return false
    return this.trim().startsWith("{")
}