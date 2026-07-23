package com.tencent.news.core.kmkv

import com.kuikly.thirdparty.kmp.lib.mmkv.mmkvWithID
import com.tencent.news.core.platform.qnFileLog

/**
 * Compatibility access for MMKV data shared with the legacy app.
 *
 * Android uses the legacy Tencent MMKV instance first so login state can be
 * shared across new/old APK overwrite installs. Other platforms keep the KMP
 * MMKV fallback.
 */
object LegacyMmkvCompat {
    private const val TAG = "LegacyMmkvCompat"

    fun getString(tableName: String, key: String, cryptKey: String? = null): String? {
        PlatformLegacyMmkvCompat.getString(tableName, key, cryptKey)?.let {
            return it
        }
        return runCatching {
            val mmkv = if (cryptKey == null) {
                mmkvWithID(mmapId = tableName)
            } else {
                mmkvWithID(mmapId = tableName, cryptKey = cryptKey)
            }
            mmkv
                .getString(key, "")
                .takeIf { it.isNotEmpty() }
        }.onFailure { error ->
            qnFileLog()?.logE(TAG, "read legacy string failed: table=$tableName key=$key", error)
        }.getOrNull()
    }

    fun getByteArray(tableName: String, key: String): ByteArray? {
        PlatformLegacyMmkvCompat.getByteArray(tableName, key)?.let {
            return it
        }
        return runCatching {
            mmkvWithID(mmapId = tableName)
                .getByteArray(key)
                ?.takeIf { it.isNotEmpty() }
        }.onFailure { error ->
            qnFileLog()?.logE(TAG, "read legacy bytes failed: table=$tableName key=$key", error)
        }.getOrNull()
    }

    fun putString(tableName: String, key: String, value: String, cryptKey: String? = null) {
        if (PlatformLegacyMmkvCompat.putString(tableName, key, value, cryptKey)) {
            return
        }
        putStringKmpOnly(tableName, key, value, cryptKey)
    }

    fun putStringKmpOnly(tableName: String, key: String, value: String, cryptKey: String? = null) {
        runCatching {
            val mmkv = if (cryptKey == null) {
                mmkvWithID(mmapId = tableName)
            } else {
                mmkvWithID(mmapId = tableName, cryptKey = cryptKey)
            }
            mmkv[key] = value
        }.onFailure { error ->
            qnFileLog()?.logE(TAG, "write legacy string failed: table=$tableName key=$key", error)
        }
    }

    fun putStringStrictNative(
        tableName: String,
        key: String,
        value: String,
        cryptKey: String? = null,
    ): LegacyMmkvWriteResult {
        val result = PlatformLegacyMmkvCompat.putStringStrictNative(tableName, key, value, cryptKey)
        if (result.supported && !result.success) {
            qnFileLog()?.logW(
                TAG,
                "strict native write failed: table=$tableName key=$key supported=${result.supported} " +
                    "stored=${result.stored} readBackMatched=${result.readBackMatched} " +
                    "readBackLen=${result.readBackLength} reason=${result.reason}"
            )
        }
        return result
    }

    fun putByteArray(tableName: String, key: String, value: ByteArray) {
        if (PlatformLegacyMmkvCompat.putByteArray(tableName, key, value)) {
            return
        }
        runCatching {
            mmkvWithID(mmapId = tableName)[key] = value
        }.onFailure { error ->
            qnFileLog()?.logE(TAG, "write legacy bytes failed: table=$tableName key=$key", error)
        }
    }

    fun removeValue(tableName: String, key: String, cryptKey: String? = null) {
        PlatformLegacyMmkvCompat.removeValue(tableName, key, cryptKey)
        runCatching {
            val mmkv = if (cryptKey == null) {
                mmkvWithID(mmapId = tableName)
            } else {
                mmkvWithID(mmapId = tableName, cryptKey = cryptKey)
            }
            mmkv.removeValueForKey(key)
        }.onFailure { error ->
            qnFileLog()?.logE(TAG, "remove legacy value failed: table=$tableName key=$key", error)
        }
    }
}

data class LegacyMmkvWriteResult(
    val supported: Boolean,
    val success: Boolean,
    val stored: Boolean,
    val readBackMatched: Boolean,
    val readBackLength: Int,
    val reason: String,
) {
    companion object {
        fun unsupported(reason: String) = LegacyMmkvWriteResult(
            supported = false,
            success = false,
            stored = false,
            readBackMatched = false,
            readBackLength = 0,
            reason = reason,
        )

        fun failed(reason: String, stored: Boolean = false, readBackLength: Int = 0) = LegacyMmkvWriteResult(
            supported = true,
            success = false,
            stored = stored,
            readBackMatched = false,
            readBackLength = readBackLength,
            reason = reason,
        )

        fun success(readBackLength: Int) = LegacyMmkvWriteResult(
            supported = true,
            success = true,
            stored = true,
            readBackMatched = true,
            readBackLength = readBackLength,
            reason = "native_readback_match",
        )
    }
}

internal expect object PlatformLegacyMmkvCompat {
    fun getString(tableName: String, key: String, cryptKey: String?): String?

    fun getByteArray(tableName: String, key: String): ByteArray?

    fun putString(tableName: String, key: String, value: String, cryptKey: String?): Boolean

    fun putStringStrictNative(tableName: String, key: String, value: String, cryptKey: String?): LegacyMmkvWriteResult

    fun putByteArray(tableName: String, key: String, value: ByteArray): Boolean

    fun removeValue(tableName: String, key: String, cryptKey: String?)
}
