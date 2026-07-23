package com.tencent.news.core.ohos.setup

import com.tencent.news.core.ohos.utils.OhosCrypto
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppEncoder
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLParameter
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString

fun setupOhosAppEncoder() {
    QnPlatformLogic.appEncoder = OhosAppEncoder()
}

internal class OhosAppEncoder : IAppEncoder {
    override fun encodeBase64Bytes(bytes: ByteArray): String {
        return bytes.toByteString().base64()
    }

    override fun decodeBase64(data: String): String {
        return data.decodeBase64()?.utf8() ?: data
    }

    override fun urlEncode(data: String): String {
        // 鸿蒙原生对`- _ . ! ~ * ' ( )`不转义，但KTOR支持的是RFC 3896，仅`- _ . ~`不转义
        // 所以简单修改了下SDK的实现
        return data.encodeURLParameter()
    }

    override fun urlDecodeUtf8(data: String): String {
        return data.decodeURLQueryComponent()
    }

    override fun removeEmoji(text: String?): String? {
        return text
    }

    override fun md5(input: String): String {
        return input.encodeUtf8().md5().hex()
    }

    override fun cipherDES(key: String, iv: String, bytes: ByteArray): ByteArray {
        return try {
            // 3DES算法：要求24字节key
            val expandedKey = key.substring(0, 8) + key.substring(0, 8) + key.substring(0, 8)
            OhosCrypto().cipher3DES(expandedKey, iv, bytes)
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    override fun cipherAESEncrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        return try {
            OhosCrypto().cipherAESEncrypt(
                key = key.toUByteArray(),
                iv = iv.toUByteArray(),
                plainBytes = data.toUByteArray()
            )
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    override fun cipherAESDecrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        return try {
            OhosCrypto().cipherAESDecrypt(
                key = key.toUByteArray(),
                iv = iv.toUByteArray(),
                cipherBytes = data.toUByteArray()
            )
        } catch (e: Exception) {
            ByteArray(0)
        }
    }
}