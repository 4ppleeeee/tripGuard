@file:Suppress("NON_EXPORTABLE_TYPE")

package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.safeToBoolean
import com.tencent.news.core.extension.safeToFloat
import com.tencent.news.core.extension.safeToInt
import com.tencent.news.core.extension.safeToLong
import com.tencent.news.core.platform.QnPlatformLogic


interface IStorage {

    fun setKV(tableName: String, key: String, value: String)

    fun getKV(tableName: String, key: String, defaultValue: String = ""): String

    fun removeValue(tableName: String, key: String)

    fun getAllKeys(tableName: String): List<String>

    fun getAll(tableName: String): Map<String, String>

    fun clearKV(tableName: String)

}


fun appStorage(): IStorage {
    return QnPlatformLogic.appStorage ?: memoryAppStorage
}

private val memoryAppStorage by lazy { MemoryAppStorage() }

class MemoryAppStorage : IStorage {

    private val memoryAppStorage = mutableMapOf<String, MutableMap<String, String>>()

    override fun setKV(tableName: String, key: String, value: String) {
        getTableMap(tableName)[key] = value
    }

    override fun getKV(tableName: String, key: String, defaultValue: String): String {
        return getTableMap(tableName).getOrElse(key) { defaultValue }
    }

    override fun removeValue(tableName: String, key: String) {
        getTableMap(tableName).remove(key)
    }

    override fun getAllKeys(tableName: String): List<String> {
        return getTableMap(tableName).keys.toList()
    }

    override fun getAll(tableName: String): Map<String, String> {
        return getTableMap(tableName)
    }

    override fun clearKV(tableName: String) {
        memoryAppStorage.remove(tableName)
    }

    private fun getTableMap(tableName: String): MutableMap<String, String> {
        val result = memoryAppStorage[tableName] ?: mutableMapOf()
        memoryAppStorage[tableName] = result
        return result
    }
}


open class DefaultAppStorage : IStorage {
    override fun setKV(tableName: String, key: String, value: String) {
    }

    override fun getKV(tableName: String, key: String, defaultValue: String): String {
        return defaultValue
    }

    override fun removeValue(tableName: String, key: String) {
    }

    override fun getAllKeys(tableName: String): List<String> {
        return getAllKeysArray(tableName).toList()
    }

    fun getAllKeysArray(tableName: String): Array<String> {
        return emptyArray()
    }

    override fun getAll(tableName: String): Map<String, String> {
        return emptyMap()
    }

    override fun clearKV(tableName: String) {
    }
}

fun IStorage.getTable(tableName: String) = StorageTable(tableName)

class StorageTable(private val tableName: String) {

    fun putString(key: String, value: String) {
        appStorage().setKV(tableName, key, value)
    }

    fun getString(key: String, def: String): String {
        return appStorage().getKV(tableName, key, def)
    }

    fun putInt(key: String, def: Int) {
        appStorage().setKV(tableName, key, def.toString())
    }

    fun putFloat(key: String, def: Float) {
        appStorage().setKV(tableName, key, def.toString())
    }

    fun putLong(key: String, def: Long) {
        appStorage().setKV(tableName, key, def.toString())
    }

    fun putBoolean(key: String, def: Boolean) {
        appStorage().setKV(tableName, key, def.toString())
    }

    fun putStringSet(key: String, def: Set<String>) {
        appStorage().setKV(tableName, key, def.joinToString(","))
    }

    fun getInt(key: String, def: Int): Int {
        return appStorage().getKV(tableName, key).safeToInt(def)
    }

    fun getFloat(key: String, def: Float): Float {
        return appStorage().getKV(tableName, key).safeToFloat(def)
    }

    fun getLong(key: String, def: Long): Long {
        return appStorage().getKV(tableName, key).safeToLong(def)
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return appStorage().getKV(tableName, key).safeToBoolean(def)
    }

    fun getStringSet(key: String, def: Set<String>?): Set<String>? {
        return appStorage().getKV(tableName, key).split(",")?.toSet() ?: def
    }

    fun remove(key: String) {
        appStorage().removeValue(tableName, key)
    }

    fun clear() {
        appStorage().clearKV(tableName)
    }

}
