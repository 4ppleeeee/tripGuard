package com.tencent.news.core.kmkv

import com.kuikly.thirdparty.kmp.lib.mmkv.MMKV_KMP
import com.kuikly.thirdparty.kmp.lib.mmkv.mmkvWithID
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IStorage

/**
 * Sets up the MMKV-backed storage implementation.
 *
 * Must be called after the platform-side MMKV initialization:
 *   - Android: `MMKV.initialize(app)` (AndroidKmkvInitTask)
 *   - iOS: `MMKV.initialize(rootDir: nil)` (IOSMmkvSetup)
 *   - HarmonyOS: `MMKV.initialize(context)` (MmkvManager.initMmkv)
 *
 * 三端统一不传 rootPath，各自走平台 init 后 mmkv 默认根目录。
 */
fun setupKmkvStorage() {
    QnPlatformLogic.appStorage = KmkvStorage()
}

/**
 * IStorage implementation backed by mmkvKotlin (cross-platform MMKV).
 * Shared across Android, iOS, and HarmonyOS via a single commonMain implementation.
 * Each tableName maps to a separate MMKV instance.
 */
internal class KmkvStorage : IStorage {

    override fun setKV(tableName: String, key: String, value: String) {
        getMmkv(tableName)[key] = value
    }

    override fun getKV(tableName: String, key: String, defaultValue: String): String {
        return getMmkv(tableName).getString(key, defaultValue)
    }

    override fun removeValue(tableName: String, key: String) {
        getMmkv(tableName).removeValueForKey(key)
    }

    override fun getAllKeys(tableName: String): List<String> {
        return getMmkv(tableName).allKeys()
    }

    override fun getAll(tableName: String): Map<String, String> {
        val mmkv = getMmkv(tableName)
        return mmkv.allKeys().associateWith { key -> mmkv.getString(key) }
    }

    override fun clearKV(tableName: String) {
        getMmkv(tableName).clearAll()
    }

    private fun getMmkv(tableName: String): MMKV_KMP = mmkvWithID(mmapId = tableName)
}
