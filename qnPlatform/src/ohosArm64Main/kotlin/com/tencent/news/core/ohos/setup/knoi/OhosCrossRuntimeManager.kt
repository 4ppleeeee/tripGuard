package com.tencent.news.core.knoi

import com.tencent.news.core.platform.ConcurrentMap
import com.tencent.news.core.platform.api.appStorage

/**
 * 如果获取到的字符串不为空，就加入缓存并返回，否则调用callee
 * @param key 缓存的key，为空则使用callee的类名
 * @param diskCache 是否启用磁盘缓存
 * @param callee 待调用的方法
 */
internal fun cacheIfNotEmpty(
    key: String? = null,
    diskCache: Boolean = false,
    callee: () -> String
): String {
    return OhosCrossRuntimeManager.StringImpl.cacheOr(key, diskCache, callee)
}

/**
 * 如果获取到的整数不为0，就加入缓存并返回，否则调用callee
 * @param key 缓存的key，为空则使用callee的类名
 * @param diskCache 是否启用磁盘缓存
 * @param callee 待调用的方法
 */
internal fun cacheIfNotZero(
    key: String? = null,
    diskCache: Boolean = false,
    callee: () -> Int
): Int {
    return OhosCrossRuntimeManager.IntImpl.cacheOr(key, diskCache, callee)
}

/**
 * 如果获取到的布尔值不为null，就加入缓存并返回，否则调用callee
 * @param key 缓存的key，为空则使用callee的类名
 * @param diskCache 是否启用磁盘缓存
 * @param callee 待调用的方法
 */
internal fun cacheIfExist(
    key: String? = null,
    diskCache: Boolean = false,
    callee: () -> Boolean
): Boolean {
    return OhosCrossRuntimeManager.BooleanImpl.cacheOr(key, diskCache, callee)
}

/**
 * App冷启生命周期内静态常量缓存
 */
private abstract class OhosCrossRuntimeManager<VType> {

    private val diskCacheKey = "ohos_cross_runtime_disk_cache"

    val cache: ConcurrentMap<String, VType> = ConcurrentMap()

    fun cacheOr(key: String?, enableDiskCache: Boolean, callee: () -> VType): VType {
        val realKey = key ?: callee.fullNameWithoutHash()

        // 内存缓存
        val memoryCacheValue = cache[realKey]
        if (memoryCacheValue != null && isExpectedValue(memoryCacheValue)) {
            return memoryCacheValue
        }

        // 磁盘缓存
        val diskCache = runIf(enableDiskCache) {
            cast(appStorage().getKV(diskCacheKey, realKey))
        }

        if (diskCache != null && isExpectedValue(diskCache)) {
            cache.put(realKey, diskCache)
            return diskCache
        }

        // 跨Runtime调用
        val answer = callee()
        cache.put(realKey, answer)
        return answer
    }

    private fun Any.fullNameWithoutHash(): String {
        return this.toString().substringBefore("@")
    }

    private inline fun <T, R> T.runIf(predict: Boolean, block: T.() -> R): R? {
        if (predict) {
            return run(block)
        }
        return null
    }

    abstract fun isExpectedValue(value: VType?): Boolean

    abstract fun cast(value: String?): VType?

    object StringImpl : OhosCrossRuntimeManager<String>() {
        override fun isExpectedValue(value: String?): Boolean {
            return !value.isNullOrEmpty()
        }

        override fun cast(value: String?): String? = value
    }

    object IntImpl : OhosCrossRuntimeManager<Int>() {
        override fun isExpectedValue(value: Int?): Boolean {
            return value != null && value != 0
        }

        override fun cast(value: String?): Int? = value?.toIntOrNull()
    }

    object BooleanImpl : OhosCrossRuntimeManager<Boolean>() {
        override fun isExpectedValue(value: Boolean?): Boolean {
            return value != null
        }

        override fun cast(value: String?): Boolean? = value?.toBoolean()
    }
}
