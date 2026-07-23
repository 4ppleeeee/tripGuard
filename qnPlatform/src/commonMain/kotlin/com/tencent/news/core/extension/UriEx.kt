package com.tencent.news.core.extension

import com.tencent.news.core.platform.api.IKtMutableUri
import com.tencent.news.core.platform.api.appEncoder
import com.tencent.news.core.platform.api.appUri


// 拼接url的path（会处理 / 的问题）
fun String.concatUriPath(path: String?): String {
    val host = this
    if (path.isNullOrEmpty()) {
        return host
    }

    val safeHost = host.concatSuffix("/")
    val safePath = path.removePrefix("/")
    return safeHost + safePath
}

// 拼接url的参数（会处理 ?或& 的问题）
fun String.concatUriParams(key: String, value: String): String {
    val url = this

    val encodeValue = appEncoder()?.urlEncode(value)?.takeIfNotBlank() ?: value

    val params = "${key}=${encodeValue}"
    return if (url.endsWith("?")) {
        url + params
    } else if (url.contains("?")) {
        url.concatSuffix("&") + params
    } else {
        url.concatSuffix("?") + params
    }
}

// 拼接url的参数（会处理 ?或& 的问题）
fun String.concatUriParams(params: Map<String, Any?>?): String {
    params ?: return this

    var result = this
    params.forEach {
        // value可能为空，导致np
        if (it.value == null) {
            // continue
            return@forEach
        }
        result = result.concatUriParams(it.key, it.value.toString())
    }
    return result
}

fun String.isHttpUrl(): Boolean {
    val url = this.lowercase()
    return url.startsWith("http://") || url.startsWith("https://")
}

fun String.takeIfHttpUrl(): String? = takeIf { it.isHttpUrl() }

fun String.replaceQueryParam(key: String, value: String?): String =
    appendQueryParam(key, value, putIfEmpty = true, putIfAbsent = false)

fun String.removeQueryParam(key: String): String = replaceQueryParam(key, "")

fun String.appendQueryParam(
    key: String,
    value: String?,
    putIfEmpty: Boolean = false,
    putIfAbsent: Boolean = false,
): String {
    return kotlin.runCatching {
        withMutableUri {
            safeAppendQuery(key, value, putIfEmpty, putIfAbsent)
        }.asString()
    }.getOrElse {
        // 理论上不会crash，这里主要是应对单测，Android系统库没有mock会crash
        concatUriParams(key, value.getNonNull())
    }
}

fun String.appendQueryParams(
    params: Map<String, String?>?,
    putIfEmpty: Boolean = false,
    putIfAbsent: Boolean = false,
): String {
    params ?: return this
    return runCatching {
        withMutableUri {
            params.forEach {
                safeAppendQuery(it.key, it.value, putIfEmpty, putIfAbsent)
            }
        }.asString()
    }.getOrElse {
        // 理论上不会crash，这里主要是应对单测，Android系统库没有mock会crash
        var result = this
        params.forEach {
            result = concatUriParams(it.key, it.value.getNonNull())
        }
        result
    }
}

private inline fun String.withMutableUri(block: IKtMutableUri.() -> Unit): IKtMutableUri =
    appUri().parseUri(this).mutate().also(block)

private fun IKtMutableUri.safeAppendQuery(
    key: String,
    value: String?,
    putIfEmpty: Boolean = false,
    putIfAbsent: Boolean = false,
) {
    if (!putIfEmpty && value.isNullOrEmpty()) {
        return
    }
    appendQuery(key, value.getNonNull(), putIfAbsent)
}