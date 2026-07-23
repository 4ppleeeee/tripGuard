package com.tencent.news.core.kmkv

internal actual object PlatformLegacyMmkvCompat {
    actual fun getString(tableName: String, key: String, cryptKey: String?): String? = null

    actual fun getByteArray(tableName: String, key: String): ByteArray? = null

    actual fun putString(tableName: String, key: String, value: String, cryptKey: String?): Boolean = false

    actual fun putStringStrictNative(
        tableName: String,
        key: String,
        value: String,
        cryptKey: String?,
    ): LegacyMmkvWriteResult = LegacyMmkvWriteResult.unsupported("strict_native_mmkv_unsupported")

    actual fun putByteArray(tableName: String, key: String, value: ByteArray): Boolean = false

    actual fun removeValue(tableName: String, key: String, cryptKey: String?) = Unit
}
