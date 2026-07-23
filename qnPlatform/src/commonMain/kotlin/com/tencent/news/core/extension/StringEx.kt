package com.tencent.news.core.extension


fun Any?.safeToString(): String = this?.toString().getNonNull()

inline fun String?.safeToInt(defaultValue: Int = 0): Int =
    safeTo(defaultValue) { toDoubleOrNull()?.toInt() }  // 兼容小数解析

inline fun String?.safeToLong(defaultValue: Long = 0): Long =
    safeTo(defaultValue) { toDoubleOrNull()?.toLong() } // 兼容小数解析

inline fun String?.safeToFloat(defaultValue: Float = 0f): Float =
    safeTo(defaultValue) { toFloatOrNull() }

inline fun String?.safeToDouble(defaultValue: Double = 0.0): Double =
    safeTo(defaultValue) { toDoubleOrNull() }

inline fun String?.safeToBoolean(defaultValue: Boolean = false): Boolean =
    safeTo(defaultValue) { toBooleanStrictOrNull() }

inline fun <T> String?.safeTo(defaultValue: T, action: String.() -> T?): T {
    if (this.isNullOrBlank()) {
        return defaultValue
    }
    return kotlin.runCatching { action() }.getOrNull() ?: defaultValue
}

fun String?.splitList(): List<String> = this?.split(",") ?: emptyList()

fun List<String>.toIntList(): List<Int> = map { it.safeToInt() }

inline fun String?.takeIfNotBlank() = takeIf { !it.isNullOrBlank() }

inline fun String?.takeIfNotEmpty() = takeIf { !it.isNullOrEmpty() }

inline fun String?.isNotNullOrEmpty() = !isNullOrEmpty()

inline fun String?.ifNotNullOrEmpty(action: (String) -> Unit) {
    if (!isNullOrEmpty()) action(this)
}

inline fun String?.isNotNullOrBlank() = !isNullOrBlank()

inline fun String?.findStr(pattern: String): String {
    if (this.isNullOrEmpty()) return ""

    val regex = Regex(pattern)
    return regex.find(this)?.run {
        groupValues.getOrNull(0)
    } ?: ""
}

inline fun String?.nonNullEquals(that: String?): Boolean = (this.isNotNullOrEmpty() && this == that)

inline fun String?.getNonNull(): String = this ?: ""

inline fun String?.getWithNullDefault(default: String): String =
    if (this.isNullOrEmpty()) default else this

inline fun String?.safeStartWith(prefix: String): Boolean = (this?.startsWith(prefix) ?: false)

fun String?.splitByLength(limit: Int): List<String> {
    val result = mutableListOf<String>()
    if (this.isNullOrEmpty() || limit <= 0) {
        return result
    }
    val strLength = length
    var size = strLength / limit
    if (strLength % limit != 0) {
        size += 1
    }
    for (i in 0 until size) {
        val startIndex = i * limit
        val endIndex = ((i + 1) * limit).coerceAtMost(strLength)
        result.add(substring(startIndex, endIndex))
    }
    return result
}

fun String?.splitByDelimiter(
    firstDelimiter: String = ",",
    secondDelimiters: String = ":",
): List<List<String>>? {
    this ?: return null

    return this.split(firstDelimiter, secondDelimiters).map { it.trim() }.chunked(2)
}

inline infix fun CharSequence.safeMatches(regex: Regex): Boolean = runCatching {
    this.matches(regex)
}.getOrElse { false }


/**
 * 校验String 类型的 RGB色值和 ARGB色值
 */
fun String?.isValidStrColor(): Boolean {
    val colorString = this
    if (colorString.isNullOrEmpty()) {
        return false
    }
    val strWithoutShape = if (colorString[0] == '#') {
        colorString.substring(1)
    } else {
        colorString
    }

    val rgbColorLength = 6
    val argbColorLength = 8

    // 兼容"#FFFFFF" 和 "#FF000000"
    if (strWithoutShape.length != rgbColorLength && strWithoutShape.length != argbColorLength) {
        return false
    }
    for (c in strWithoutShape.toCharArray()) {
        if ((c < '0' || c > '9') && (c < 'A' || c > 'F') && (c < 'a' || c > 'f')) {
            return false
        }
    }
    return true
}

fun String?.getQueryParam(key: String?): String {
    val url = this
    if (url.isNullOrEmpty() || key.isNullOrEmpty()) {
        return ""
    }

    val params = url.substringAfterLast("?").split("&")

    val prefix = "${key}="
    val param = params.find { it.startsWith(prefix) }

    return param?.removePrefix(prefix) ?: ""
}

fun String?.safeSubString(subLength: Int): String {
    return if (this.isNullOrEmpty() || this.length <= subLength) {
        this ?: ""
    } else {
        substring(0, subLength)
    }
}

fun String.concatSuffix(
    suffix: String,
    concatIfEmpty: Boolean = false,
): String {
    if (isEmpty() && !concatIfEmpty) {
        return this
    }
    if (endsWith(suffix)) {
        return this
    }
    return this + suffix
}

fun String.concatPrefix(
    prefix: String,
    div: String = "",
    concatIfEmpty: Boolean = false,
): String {
    if (isEmpty() && !concatIfEmpty) {
        return this
    }
    if (startsWith(prefix)) {
        return this
    }
    return prefix + div + this
}

fun String.ellipsizeEnd(size: Int, suffix: String = "..."): String {
    if (length > size) {
        return substring(0, size).concatSuffix(suffix)
    }
    return this
}

fun getFirstNonNullString(vararg strings: String?): String {
    if (strings.isEmpty()) {
        return ""
    }
    for (str in strings) {
        if (!str.isNullOrEmpty()) {
            return str
        }
    }
    return ""
}

fun Boolean?.toIntString(): String {
    this ?: return "0"
    return if (this) "1" else "0"
}

// 格式化时长：HH:mm:ss
fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return ""

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    // 补零操作
    fun pad(num: Long): String = num.toString().padStart(2, '0')

    return if (hours > 0) {
        "${hours}:${pad(minutes)}:${pad(remainingSeconds)}"
    } else {
        "${pad(minutes)}:${pad(remainingSeconds)}"
    }
}

fun isUrl(str: String): Boolean {
    return str.isNotNullOrEmpty() && str.startsWith("http")
}