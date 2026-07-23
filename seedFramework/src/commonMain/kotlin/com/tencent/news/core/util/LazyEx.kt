package com.tencent.news.core.util

import com.tencent.news.core.platform.api.isDebug
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object LazyEx {

    fun <T> lazyForRelease(initializer: () -> T): ReadOnlyProperty<Any?, T> {
        return LazyForReleaseDelegate(initializer)
    }

    private class LazyForReleaseDelegate<T>(
        private val initializer: () -> T
    ) : ReadOnlyProperty<Any?, T> {

        // 用于 release 模式的缓存值
        private var cachedValue: T? = null

        // 标记是否已经初始化过（仅在 release 模式使用）
        private var isInitialized = false

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            // Debug 模式：每次都重新计算
            if (isDebug()) {
                return initializer()
            }

            // Release 模式：使用缓存
            if (!isInitialized) {
                cachedValue = initializer()
                isInitialized = true
            }

            @Suppress("UNCHECKED_CAST")
            return cachedValue as T
        }
    }
}