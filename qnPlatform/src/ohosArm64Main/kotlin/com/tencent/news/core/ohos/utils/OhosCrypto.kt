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
 */
internal class OhosCrypto {

    var keyGenPtr: CPointerVar<OH_CryptoSymKeyGenerator>? = null
    var symKeyPtr: CPointerVar<OH_CryptoSymKey>? = null
    var cipherPtr: CPointerVar<OH_CryptoSymCipher>? = null
    var paramsPtr: CPointerVar<OH_CryptoSymCipherParams>? = null
    var cipherBlob: Crypto_DataBlob? = null

    /**
     * 使用3DES CBC模式加密数据
     * @see cipher3DES
     */
    fun cipher3DES(key: String, iv: String, bytes: ByteArray): ByteArray {
        return memScoped {
            this@memScoped.run {
                cipher3DES(key.toUByteArray(), iv.toUByteArray(), bytes.toUByteArray())
            }
        }
    }

    /**
     * 使用3DES CBC模式加密数据
     * 参考文档：https://developer.huawei.com/consumer/cn/doc/harmonyos-guides/crypto-3des-sym-encrypt-decrypt-ecb-ndk
     *
     * @param key 24字节的密钥
     * @param iv 8字节的初始化向量（CBC模式必需）
     * @param plainBytes 待加密的明文数据
     * @return 加密后的密文数据，失败返回空数组
     */
    fun AutofreeScope.cipher3DES(
        key: UByteArray,
        iv: UByteArray,
        plainBytes: UByteArray
    ): ByteArray {

        try {
            // 步骤1：创建并转换密钥
            if (!createSymKey(key)) {
                return ByteArray(0)
            }

            // 步骤2：创建加密器
            if (!createCipher()) {
                return ByteArray(0)
            }

            // 步骤3：设置IV并初始化
            if (!initCipherWithIV(iv)) {
                return ByteArray(0)
            }

            // 步骤4：执行加密
            return performEncryption(plainBytes)

        } finally {
            // 统一释放资源
            this@OhosCrypto.release()
        }
    }

    /**
     * 步骤1：创建密钥生成器并转换密钥
     */
    private fun AutofreeScope.createSymKey(key: UByteArray): Boolean {
        val keyGenPtr = alloc<CPointerVar<OH_CryptoSymKeyGenerator>>()
        this@OhosCrypto.keyGenPtr = keyGenPtr
        if (OH_CryptoSymKeyGenerator_Create("3DES192", keyGenPtr.ptr) != 0u) {
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
     */
    private fun AutofreeScope.createCipher(): Boolean {
        val cipherPtr = alloc<CPointerVar<OH_CryptoSymCipher>>()
        this@OhosCrypto.cipherPtr = cipherPtr
        return OH_CryptoSymCipher_Create("3DES192|CBC|PKCS7", cipherPtr.ptr) == 0u
    }

    /**
     * 步骤3：设置IV并初始化加密器
     */
    private fun AutofreeScope.initCipherWithIV(iv: UByteArray): Boolean {
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

        // 初始化加密模式
        val cipherPtr = this@OhosCrypto.cipherPtr ?: return false
        val symKeyPtr = this@OhosCrypto.symKeyPtr ?: return false
        return OH_CryptoSymCipher_Init(
            cipherPtr.value,
            CRYPTO_ENCRYPT_MODE,
            symKeyPtr.value,
            paramsPtr.value
        ) == 0u
    }

    /**
     * 步骤4：执行加密操作
     */
    private fun AutofreeScope.performEncryption(plainBytes: UByteArray): ByteArray {
        // 准备明文数据
        val plainBlob = alloc<Crypto_DataBlob>().apply {
            data = plainBytes.refTo(0).getPointer(this@performEncryption)
            len = plainBytes.size.toULong()
        }

        val cipherPtr = this@OhosCrypto.cipherPtr ?: return ByteArray(0)

        // 执行加密
        val cipherBlob = alloc<Crypto_DataBlob>()
        if (OH_CryptoSymCipher_Final(cipherPtr.value, plainBlob.ptr, cipherBlob.ptr) != 0u) {
            return ByteArray(0)
        }

        val data = cipherBlob.data ?: return ByteArray(0)

        // 复制加密结果
        this@OhosCrypto.cipherBlob = cipherBlob
        return ByteArray(cipherBlob.len.toInt()) { i -> data[i].toByte() }
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
}