package com.tencent.news.core.kmkv

import com.tencent.news.core.platform.qnFileLog

internal actual object PlatformLegacyMmkvCompat {
    private const val TAG = "PlatformLegacyMmkvCompat"

    actual fun getString(tableName: String, key: String, cryptKey: String?): String? {
        return runLegacy("getString", tableName, key) {
            val mmkv = mmkvWithId(tableName, cryptKey) ?: return@runLegacy null
            val value = mmkv.javaClass
                .getMethod("decodeString", String::class.java, String::class.java)
                .invoke(mmkv, key, "") as? String
            value?.takeIf { it.isNotEmpty() }
        }
    }

    actual fun getByteArray(tableName: String, key: String): ByteArray? {
        return runLegacy("getByteArray", tableName, key) {
            val mmkv = mmkvWithId(tableName, null) ?: return@runLegacy null
            (mmkv.javaClass.getMethod("decodeBytes", String::class.java).invoke(mmkv, key) as? ByteArray)
                ?.takeIf { it.isNotEmpty() }
        }
    }

    actual fun putString(tableName: String, key: String, value: String, cryptKey: String?): Boolean {
        return runLegacy("putString", tableName, key) {
            val mmkv = mmkvWithId(tableName, cryptKey) ?: return@runLegacy false
            val stored = mmkv.encodeString(key, value)
            mmkv.sync()
            stored
        } ?: false
    }

    actual fun putStringStrictNative(
        tableName: String,
        key: String,
        value: String,
        cryptKey: String?,
    ): LegacyMmkvWriteResult {
        return runCatching {
            val mmkv = mmkvWithId(tableName, cryptKey)
                ?: return@runCatching LegacyMmkvWriteResult.failed("mmkv_instance_null")
            mmkv.checkContentChangedByOuterProcess()
            val stored = mmkv.encodeString(key, value)
            mmkv.sync()
            mmkv.checkContentChangedByOuterProcess()
            val readBack = mmkv.decodeString(key, "")
            val readBackLength = readBack?.length ?: 0
            val result = when {
                stored && readBack == value -> LegacyMmkvWriteResult.success(readBackLength)
                stored -> LegacyMmkvWriteResult.failed(
                    reason = "native_readback_mismatch",
                    stored = true,
                    readBackLength = readBackLength,
                )
                else -> LegacyMmkvWriteResult.failed(
                    reason = "native_encode_return_false",
                    stored = false,
                    readBackLength = readBackLength,
                )
            }
            logStrictWriteResult(tableName, key, value.length, cryptKey != null, result)
            result
        }.getOrElse { error ->
            qnFileLog()?.logE(TAG, "putStringStrictNative failed: table=$tableName key=$key", error)
            LegacyMmkvWriteResult.failed("native_exception:${error.javaClass.simpleName}")
        }
    }

    actual fun putByteArray(tableName: String, key: String, value: ByteArray): Boolean {
        return runLegacy("putByteArray", tableName, key) {
            val mmkv = mmkvWithId(tableName, null) ?: return@runLegacy false
            val stored = mmkv.javaClass
                .getMethod("encode", String::class.java, ByteArray::class.java)
                .invoke(mmkv, key, value) as? Boolean ?: false
            mmkv.sync()
            stored
        } ?: false
    }

    actual fun removeValue(tableName: String, key: String, cryptKey: String?) {
        runLegacy("removeValue", tableName, key) {
            val mmkv = mmkvWithId(tableName, cryptKey) ?: return@runLegacy
            mmkv.javaClass.getMethod("removeValueForKey", String::class.java).invoke(mmkv, key)
            mmkv.sync()
        }
    }

    private fun mmkvWithId(tableName: String, cryptKey: String?): Any? {
        val clazz = Class.forName("com.tencent.mmkv.MMKV")
        val mode = clazz.getField("MULTI_PROCESS_MODE").getInt(null)
        return if (cryptKey == null) {
            clazz.getMethod("mmkvWithID", String::class.java, Integer.TYPE)
                .invoke(null, tableName, mode)
        } else {
            clazz.getMethod("mmkvWithID", String::class.java, Integer.TYPE, String::class.java)
                .invoke(null, tableName, mode, cryptKey)
        }
    }

    private fun Any.sync() {
        javaClass.getMethod("sync").invoke(this)
    }

    private fun Any.encodeString(key: String, value: String): Boolean {
        return javaClass
            .getMethod("encode", String::class.java, String::class.java)
            .invoke(this, key, value) as? Boolean ?: false
    }

    private fun Any.decodeString(key: String, defaultValue: String): String? {
        return javaClass
            .getMethod("decodeString", String::class.java, String::class.java)
            .invoke(this, key, defaultValue) as? String
    }

    private fun Any.checkContentChangedByOuterProcess() {
        runCatching {
            javaClass.getMethod("checkContentChangedByOuterProcess").invoke(this)
        }
    }

    private fun logStrictWriteResult(
        tableName: String,
        key: String,
        valueLength: Int,
        encrypted: Boolean,
        result: LegacyMmkvWriteResult,
    ) {
        val msg = "putStringStrictNative result: table=$tableName key=$key valueLen=$valueLength " +
            "encrypted=$encrypted stored=${result.stored} readBackMatched=${result.readBackMatched} " +
            "readBackLen=${result.readBackLength} reason=${result.reason}"
        if (result.success) {
            qnFileLog()?.logI(TAG, msg)
        } else {
            qnFileLog()?.logW(TAG, msg)
        }
    }

    private inline fun <T> runLegacy(scene: String, tableName: String, key: String, block: () -> T): T? {
        return runCatching(block)
            .onFailure { error ->
                qnFileLog()?.logE(TAG, "$scene failed: table=$tableName key=$key", error)
            }
            .getOrNull()
    }
}
