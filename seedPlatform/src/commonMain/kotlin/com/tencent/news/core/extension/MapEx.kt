package com.tencent.news.core.extension

typealias StringMap = Map<String, String>
typealias MutableStringMap = MutableMap<String, String>

fun mutableStringMapOf(vararg pairs: Pair<String, String>): MutableStringMap = mutableMapOf(*pairs)

fun <T> MutableMap<T, Int>.increaseCount(key: T): Int {
    val curCount = this[key] ?: 0
    val newCount = curCount + 1
    this[key] = newCount
    return newCount
}

fun <K, V> MutableMap<K, V>?.safePutAll(from: Map<out K, V>?): MutableMap<K, V>? {
    if (this != null && from != null) {
        this.putAll(from)
    }
    return this
}

fun <K, V> Map<K, V?>?.noneNullMap(): MutableMap<K, V> {
    val result = mutableMapOf<K, V>()
    this?.safeForEach {
        val value = it.value
        if (value != null) {
            result[it.key] = value
        }
    }
    return result
}

fun <K, V> Map<K?, V?>?.noKVNullMap(): Map<K, V> {
    val result = mutableMapOf<K, V>()
    this?.safeForEach {
        val value = it.value
        val key = it.key
        if (key != null && value != null) {
            result[key] = value
        }
    }
    return result
}

fun Map<String, String?>?.noneNullStringMap(): MutableMap<String, String> {
    val result = mutableMapOf<String, String>()
    this?.safeForEach {
        val value = it.value
        if (!value.isNullOrEmpty()) {
            result[it.key] = value
        }
    }
    return result
}

inline fun <K, V> Map<K, V>?.safeForEach(action: (Map.Entry<K, V>) -> Unit) {
    this?.entries?.safeForEach(action) // 防止ConcurrentModify
}


fun Map<String, Any>.toIntMap(): Map<String, Int> {
    return this.mapNotNull { (key, value) ->
        val intValue = when (value) {
            is Int -> value
            is String -> value.safeToInt()
            else -> null
        }
        intValue?.let { key to it }
    }.toMap()
}

fun Map<String, Any>.toStringMap(): Map<String, String> {
    return this.mapNotNull { (key, value) ->
        key to value.safeToString()
    }.toMap()
}

fun <T, K, V> List<T>?.convertToMap(convert: (T) -> Pair<K, V>): Map<K, V> {
    val result = mutableMapOf<K, V>()
    this?.safeForEach {
        val pair = convert(it)
        result[pair.first] = pair.second
    }
    return result
}

fun <V> MutableMap<String, V>.safePutIfAbsent(key: String, value: V): MutableMap<String, V> {
    get(key) ?: put(key, value)

    return this
}