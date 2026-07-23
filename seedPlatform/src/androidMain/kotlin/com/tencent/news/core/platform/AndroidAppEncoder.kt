package com.tencent.news.core.platform

import android.util.Base64
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.api.IAppEncoder
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

@KmmInternalApi
internal fun setupAndroidAppEncoder() {
    QnPlatformLogic.appEncoder = AndroidAppEncoder()
}

private class AndroidAppEncoder : IAppEncoder {

    override fun encodeBase64Bytes(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun decodeBase64(data: String): String {
        return try {
            String(Base64.decode(data, Base64.DEFAULT))
        } catch (e: Exception) {
            data
        }
    }

    override fun urlEncode(data: String): String {
        return URLEncoder.encode(data, "UTF-8")
    }

    override fun urlDecodeUtf8(data: String): String {
        return try {
            URLDecoder.decode(data, "UTF-8")
        } catch (e: Exception) {
            data
        }
    }

    override fun cipherDES(key: String, iv: String, bytes: ByteArray): ByteArray {
        return ByteArray(0)
    }

    override fun cipherAESEncrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        return ByteArray(0)
    }

    override fun cipherAESDecrypt(key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        return ByteArray(0)
    }

    override fun removeEmoji(text: String?): String? {
        return text
    }

    override fun md5(input: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    override fun rsaEncryptBase64(data: ByteArray, publicKey: String): String {
        return try {
            val keyBytes = Base64.decode(publicKey, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(keySpec))
            Base64.encodeToString(cipher.doFinal(data), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }
}
