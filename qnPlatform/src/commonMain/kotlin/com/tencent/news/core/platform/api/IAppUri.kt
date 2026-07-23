package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.concatPrefix
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.getPlatformUri


interface IAppUri {
    @Throws(RuntimeException::class)
    fun parseUri(uriString: String): IKtUri // 注意！非法字符可能解析报错
}

abstract class AbsAppUri : IAppUri

interface IKtUri {
    val scheme: String?
    val host: String
    val path: String

    // 返回值不需要再urlDecode，已经处理好了
    fun getQuery(key: String): String

    fun getAllQuery(): Map<String, String>

    fun mutate(): IKtMutableUri
}

interface IKtMutableUri {
    // value不需要urlEncode，内部实现会处理
    fun appendQuery(key: String, value: String, putIfAbsent: Boolean = true)

    fun build(): IKtUri
    fun asString(): String
}

abstract class AbsKtUri : IKtUri

fun appUri(): IAppUri = QnPlatformLogic.appUri ?: getPlatformUri()

fun String?.safeParseUri(): IKtUri? {
    if (this.isNullOrEmpty()) return null
    return runCatching {
        appUri().parseUri(this)
    }.getOrNull()
}

fun String?.matchUriPath(path: String): Boolean = safeParseUri()?.path == path.concatPrefix("/")