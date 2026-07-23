package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeDecodeStringMap
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.extension.safeToFloat
import com.tencent.news.core.extension.safeToInt
import com.tencent.news.core.extension.safeToLong
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.serializer.KtJson


interface IAppConfig {

    // shiply配置
    fun getShiplyConfig(key: String, defaultValue: String = ""): String

    // shiply开关
    fun getShiplySwitch(key: String, defaultValue: Boolean = false): Boolean

    @Deprecated("这个是业务逻辑，依赖关系不对，需要改造")
    fun getAppInitConfig(): IAppInitConfig?

    /**
     * 直接取tab实验配置
     * 通过shiply绑定tab实验来取开关可以覆盖大部分场景；但当tab实验配置人群标签限定adcode时，通过shiply绑定实验来取开关会失效
     * 因此提供一个api来直接取tab实验配置
     */
    fun getTabExpInt(key: String, defaultValue: Int = 0): Int
}


abstract class AbsAppConfig : IAppConfig


fun appConfig(): IAppConfig? = QnPlatformLogic.appConfig

// 【shiply配置结构体】：适用于直接配的json结构
inline fun <reified T : IKmmKeep> getShiplyParsedConfig(key: String, defaultValue: T? = null): T? {
    val json = getShiplyConfig(key).takeIfNotEmpty()
        ?: return defaultValue
    return KtJson.safeDecode<T>(json) ?: defaultValue
}

// 变体：始终返回非空列表
inline fun <reified T : IKmmKeep> getSafeConfigList(
    key: String
): List<T> = getShiplyParsedConfigList(key) ?: emptyList()

// 【常用】【shiply配置结构体】：适用于配模板结构（shiply默认都是数组）
inline fun <reified T : IKmmKeep> getShiplyParsedConfigList(
    key: String,
    defaultValue: List<T>? = null,
): List<T>? {
    val json = getShiplyConfig(key).takeIfNotEmpty()
        ?: return defaultValue
    return KtJson.safeDecode<List<T>>(json) ?: defaultValue
}

fun getShiplyStringList(key: String, defaultValue: List<String>? = null): List<String>? {
    val json = getShiplyConfig(key).takeIfNotEmpty()
        ?: return defaultValue
    return KtJson.safeDecode<List<String>>(json) ?: defaultValue
}


fun getShiplyConfig(key: String, defaultValue: String = ""): String =
    appConfig()?.getShiplyConfig(key, defaultValue) ?: defaultValue


fun getShiplySwitch(key: String, defaultValue: Boolean = false): Boolean =
    appConfig()?.getShiplySwitch(key, defaultValue) ?: defaultValue


fun getShiplyInt(key: String, defaultValue: Int = 0): Int =
    getShiplyConfig(key).safeToInt(defaultValue)


fun getShiplyFloat(key: String, defaultValue: Float = 0f): Float =
    getShiplyConfig(key).safeToFloat(defaultValue)

fun getShiplyLong(key: String, defaultValue: Long = 0): Long =
    getShiplyConfig(key).safeToLong(defaultValue)

fun getShiplyMap(key: String, def: Map<String, String>): Map<String, String> {
    return KtJson.safeDecodeStringMap(getShiplyConfig(key)) ?: def
}

// 与闪屏广告敲定的配置协议
fun getShiplyConfigMap(keyList: List<String?>?): Map<String, Map<String, String>> {
    val result = mutableMapOf<String, Map<String, String>>()
    safeList(keyList).forEach { key ->
        result[key] = mapOf(
            "switch" to if (getShiplySwitch(key)) "1" else "0",
            "value" to getShiplyConfig(key)
        )
    }
    return result
}