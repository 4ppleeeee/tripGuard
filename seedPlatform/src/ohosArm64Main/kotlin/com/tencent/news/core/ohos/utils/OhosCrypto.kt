package com.tencent.news.core.ohos.utils

import cnames.structs.OH_CryptoSymCipher
import cnames.structs.OH_CryptoSymCipherParams
import cnames.structs.OH_CryptoSymKey
import cnames.structs.OH_CryptoSymKeyGenerator
import kotlinx.cinterop.AutofreeScope
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.devices.CRYPTO_DECRYPT_MODE
import platform.devices.CRYPTO_ENCRYPT_MODE
import platform.devices.CRYPTO_IV_DATABLOB
import platform.devices.Crypto_DataBlob
import platform.devices.OH_CryptoSymCipherParams_Create
import platform.devices.OH_CryptoSymCipherParams_Destroy
import platform.devices.OH_CryptoSymCipherParams_SetParam
import platform.devices.OH_CryptoSymCipher_Create
import platform.devices.OH_CryptoSymCipher_Destroy
import platform.devices.OH_CryptoSymCipher_Final
import platform.devices.OH_CryptoSymCipher_Init
import platform.devices.OH_CryptoSymKeyGenerator_Convert
import platform.devices.OH_CryptoSymKeyGenerator_Create
import platform.devices.OH_CryptoSymKeyGenerator_Destroy
import platform.devices.OH_CryptoSymKey_Destroy
import platform.devices.OH_Crypto_FreeDataBlob

/**
 * 鸿蒙加密工具
 *
 * 基于鸿蒙 Crypto Architecture Kit NDK（C API）封装对称加密操作，
 * 支持 3DES/CBC/PKCS7 和 AES/CBC/PKCS7 两种加密模式。
 *
 * 参考文档：
 * - 3DES: https://developer.huawei.com/consumer/cn/doc/harmonyos-guides/crypto-3des-sym-encrypt-decrypt-ecb-ndk
 * - AES:  https://developer.huawei.com/consumer/cn/doc/harmonyos-guides/crypto-aes-sym-encrypt-decrypt-gcm-by-segment-ndk
 */
internal class OhosCrypto {

    private var keyGenPtr: CPointerVar<OH_CryptoSymKeyGenerator>? = null
    private var symKeyPtr: CPointerVar<OH_CryptoSymKey>? = null
    private var cipherPtr: CPointerVar<OH_CryptoSymCipher>? = null
    private var paramsPtr: CPointerVar<OH_CryptoSymCipherParams>? = null
    private var cipherBlob: Crypto_DataBlob? = null

    // ========== 3DES 加密 ==========

    /**
     * 使用3DES CBC模式加密数据
     *
     * @param key 24字节的密钥（字符串形式）
     * @param iv 8字节的初始化向量（字符串形式）
     * @param bytes 待加密的明文数据
     * @return 加密后的密文数据，失败返回空数组
     */
    fun cipher3DES(key: String, iv: String, bytes: ByteArray): ByteArray {
        return cipherSymmetric(
            keyGenAlgo = KEY_GEN_3DES192,
            cipherAlgo = CIPHER_3DES_CBC_PKCS7,
            key = key.toUByteArray(),
            iv = iv.toUByteArray(),
            plainBytes = bytes.toUByteArray(),
            isEncrypt = true
        )
    }

    // ========== AES 加密/解密 ==========

    /**
     * 使用AES128 CBC模式加密数据
     * 参考文档：https://developer.huawei.com/consumer/cn/doc/harmonyos-guides/crypto-aes-sym-encrypt-decrypt-gcm-by-segment-ndk
     *
     * @param key 16字节的密钥
     * @param iv 16字节的初始化向量（CBC模式必需）
     * @param plainBytes 待加密的明文数据
     * @return 加密后的密文数据，失败返回空数组
     */
    fun cipherAESEncrypt(key: UByteArray, iv: UByteArray, plainBytes: UByteArray): ByteArray {
        return cipherSymmetric(
            keyGenAlgo = KEY_GEN_AES128,
            cipherAlgo = CIPHER_AES128_CBC_PKCS7,
            key = key,
            iv = iv,
            plainBytes = plainBytes,
            isEncrypt = true
        )
    }

    /**
     * 使用AES128 CBC模式解密数据
     *
     * @param key 16字节的密钥
     * @param iv 16字节的初始化向量（CBC模式必需）
     * @param cipherBytes 待解密的密文数据
     * @return 解密后的明文数据，失败返回空数组
     */
    fun cipherAESDecrypt(key: UByteArray, iv: UByteArray, cipherBytes: UByteArray): ByteArray {
        return cipherSymmetric(
            keyGenAlgo = KEY_GEN_AES128,
            cipherAlgo = CIPHER_AES128_CBC_PKCS7,
            key = key,
            iv = iv,
            plainBytes = cipherBytes,
            isEncrypt = false
        )
    }

    // ========== 通用对称加密流程 ==========

    /**
     * 通用对称加密/解密流程
     *
     * @param keyGenAlgo 密钥生成器算法规格（如 "3DES192"、"AES128"）
     * @param cipherAlgo 加密器算法规格（如 "3DES192|CBC|PKCS7"、"AES128|CBC|PKCS7"）
     * @param key 密钥字节数组
     * @param iv 初始化向量字节数组
     * @param plainBytes 待处理的数据（加密时为明文，解密时为密文）
     * @param isEncrypt true 为加密，false 为解密
     * @return 处理后的数据，失败返回空数组
     */
    private fun cipherSymmetric(
        keyGenAlgo: String,
        cipherAlgo: String,
        key: UByteArray,
        iv: UByteArray,
        plainBytes: UByteArray,
        isEncrypt: Boolean
    ): ByteArray {
        return memScoped {
            try {
                // 步骤1：创建并转换密钥
                if (!createSymKey(keyGenAlgo, key)) {
                    return@memScoped ByteArray(0)
                }

                // 步骤2：创建加密器
                if (!createCipher(cipherAlgo)) {
                    return@memScoped ByteArray(0)
                }

                // 步骤3：设置IV并初始化
                if (!initCipherWithIV(iv, isEncrypt)) {
                    return@memScoped ByteArray(0)
                }

                // 步骤4：执行加密/解密
                performCryption(plainBytes)
            } finally {
                // 统一释放资源
                this@OhosCrypto.release()
            }
        }
    }

    /**
     * 步骤1：创建密钥生成器并转换密钥
     *
     * @param keyGenAlgo 密钥生成器算法规格
     * @param key 密钥字节数组
     */
    private fun AutofreeScope.createSymKey(keyGenAlgo: String, key: UByteArray): Boolean {
        val keyGenPtr = alloc<CPointerVar<OH_CryptoSymKeyGenerator>>()
        this@OhosCrypto.keyGenPtr = keyGenPtr
        if (OH_CryptoSymKeyGenerator_Create(keyGenAlgo, keyGenPtr.ptr) != 0u) {
            return false
        }

        val keyBlob = alloc<Crypto_DataBlob>().apply {
            data = key.refTo(0).getPointer(this@createSymKey)
            len = key.size.toULong()
        }

        val symKeyPtr = alloc<CPointerVar<OH_CryptoSymKey>>()
        this@OhosCrypto.symKeyPtr = symKeyPtr
        return OH_CryptoSymKeyGenerator_Convert(keyGenPtr.value, keyBlob.ptr, symKeyPtr.ptr) == 0u
    }

    /**
     * 步骤2：创建加密器对象
     *
     * @param cipherAlgo 加密器算法规格
     */
    private fun AutofreeScope.createCipher(cipherAlgo: String): Boolean {
        val cipherPtr = alloc<CPointerVar<OH_CryptoSymCipher>>()
        this@OhosCrypto.cipherPtr = cipherPtr
        return OH_CryptoSymCipher_Create(cipherAlgo, cipherPtr.ptr) == 0u
    }

    /**
     * 步骤3：设置IV并初始化加密器
     *
     * @param iv 初始化向量
     * @param isEncrypt true 为加密模式，false 为解密模式
     */
    private fun AutofreeScope.initCipherWithIV(iv: UByteArray, isEncrypt: Boolean): Boolean {
        // 创建参数对象
        val paramsPtr = alloc<CPointerVar<OH_CryptoSymCipherParams>>()
        this@OhosCrypto.paramsPtr = paramsPtr
        if (OH_CryptoSymCipherParams_Create(paramsPtr.ptr) != 0u) {
            return false
        }

        // 设置IV
        val ivBlob = alloc<Crypto_DataBlob>().apply {
            data = iv.refTo(0).getPointer(this@initCipherWithIV)
            len = iv.size.toULong()
        }
        val res = OH_CryptoSymCipherParams_SetParam(paramsPtr.value, CRYPTO_IV_DATABLOB, ivBlob.ptr)
        if (res != 0u) {
            return false
        }

        // 初始化加密/解密模式
        val cipherPtr = this@OhosCrypto.cipherPtr ?: return false
        val symKeyPtr = this@OhosCrypto.symKeyPtr ?: return false
        val mode = if (isEncrypt) CRYPTO_ENCRYPT_MODE else CRYPTO_DECRYPT_MODE
        return OH_CryptoSymCipher_Init(
            cipherPtr.value,
            mode,
            symKeyPtr.value,
            paramsPtr.value
        ) == 0u
    }

    /**
     * 步骤4：执行加密/解密操作
     *
     * @param inputBytes 待处理的数据
     */
    private fun AutofreeScope.performCryption(inputBytes: UByteArray): ByteArray {
        // 准备输入数据
        val inputBlob = alloc<Crypto_DataBlob>().apply {
            data = inputBytes.refTo(0).getPointer(this@performCryption)
            len = inputBytes.size.toULong()
        }

        val cipherPtr = this@OhosCrypto.cipherPtr ?: return ByteArray(0)

        // 执行加密/解密
        val outputBlob = alloc<Crypto_DataBlob>()
        if (OH_CryptoSymCipher_Final(cipherPtr.value, inputBlob.ptr, outputBlob.ptr) != 0u) {
            return ByteArray(0)
        }

        val data = outputBlob.data ?: return ByteArray(0)

        // 复制结果
        this@OhosCrypto.cipherBlob = outputBlob
        return ByteArray(outputBlob.len.toInt()) { i -> data[i].toByte() }
    }

    /**
     * 释放所有资源
     */
    private fun release() {
        cipherBlob?.let { OH_Crypto_FreeDataBlob(it.ptr) }
        paramsPtr?.value?.let { OH_CryptoSymCipherParams_Destroy(it) }
        cipherPtr?.value?.let { OH_CryptoSymCipher_Destroy(it) }
        symKeyPtr?.value?.let { OH_CryptoSymKey_Destroy(it) }
        keyGenPtr?.value?.let { OH_CryptoSymKeyGenerator_Destroy(it) }
    }

    companion object {
        // 密钥生成器算法规格
        private const val KEY_GEN_3DES192 = "3DES192"
        private const val KEY_GEN_AES128 = "AES128"

        // 加密器算法规格
        private const val CIPHER_3DES_CBC_PKCS7 = "3DES192|CBC|PKCS7"
        private const val CIPHER_AES128_CBC_PKCS7 = "AES128|CBC|PKCS7"
    }
}