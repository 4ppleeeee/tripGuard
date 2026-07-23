package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic


interface IAppEncoder {

    // 将字节数组解析成base64格式字符串
    fun encodeBase64Bytes(bytes: ByteArray): String

    // 解码base64字符串
    fun decodeBase64(data: String): String

    /**
     * url编码（处理参数转移问题，尽量遵守 RFC 3986 标准）
     * https://tools.ietf.org/html/rfc3986#section-2.3
     * Android: RFC 2396，`- _ . ! ~ * ' ( )` 不转义
     * Ohos:    RFC 2396, `- _ . ! ~ * ' ( )` 不转义
     * iOS:     `!*'\"();:@&=+$,/?%#[]% `不转移（注意：最后是个空格）
     */
    fun urlEncode(data: String): String

    // url UTF-8 解码
    fun urlDecodeUtf8(data: String): String

    // DES/CBC/PKCS5Padding 加密（目前主要广告在用）
    fun cipherDES(key: String, iv: String, bytes: ByteArray): ByteArray

    // 删除文本中的emoji
    fun removeEmoji(text: String?): String?

    // 计算字符串的MD5哈希值(32位十六进制字符串)
    fun md5(input: String): String

}

fun appEncoder(): IAppEncoder? = QnPlatformLogic.appEncoder?.let { AppEncoderInterceptor(it) }

fun urlEncode(data: String): String = appEncoder()?.urlEncode(data) ?: data

// 这里故意名字起的和urlEncode差异大一点，防止误用
fun urlDecodeUtf8(data: String): String = appEncoder()?.urlDecodeUtf8(data) ?: data

fun decodeBase64(data: String?): String? {
    if (data.isNullOrBlank()) {
        return data
    }
    return appEncoder()?.decodeBase64(data) ?: data
}

fun encodeBase64(data: String?): String? {
    if (data.isNullOrBlank()) {
        return null
    }
    return appEncoder()?.encodeBase64Bytes(data.encodeToByteArray())
}

fun String?.md5(): String {
    this ?: return ""
    return appEncoder()?.md5(this) ?: ""
}


fun removeContentEmoji(text: String?): String? {
    if (text.isNullOrEmpty()) {
        return text
    }
    return QnPlatformLogic.appEncoder?.removeEmoji(text)
}

private class AppEncoderInterceptor(private val target: IAppEncoder) : IAppEncoder by target {

    override fun urlEncode(data: String): String {
        val result = target.urlEncode(data)

        // 【空格转换问题】不同平台的默认实现会不同，有的是转成 +（安卓）有的是 %20（鸿蒙）
        // + 是源自早期的 HTML 和 URL 编码实践；
        // %20 是符合 RFC 3986 标准
        // kmm内统一遵守 RFC标准
        return result.replace("+", "%20")
    }

}