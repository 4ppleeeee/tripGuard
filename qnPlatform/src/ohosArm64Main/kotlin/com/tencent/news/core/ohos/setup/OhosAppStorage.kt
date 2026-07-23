package com.tencent.news.core.ohos.setup

import com.tencent.news.core.ohos.utils.using
import com.tencent.news.core.ohos.utils.usingKString
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IStorage
import com.tencent.news.qncore.ohos.MMKV_C_SINGLE_PROCESS
import com.tencent.news.qncore.ohos.mmkv_c_all_keys
import com.tencent.news.qncore.ohos.mmkv_c_clear_all
import com.tencent.news.qncore.ohos.mmkv_c_get_string
import com.tencent.news.qncore.ohos.mmkv_c_mmkv_with_id
import com.tencent.news.qncore.ohos.mmkv_c_remove_value_for_key
import com.tencent.news.qncore.ohos.mmkv_c_set_string
import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import platform.posix.free

fun setupOhosStorage() {
    QnPlatformLogic.appStorage = OhosStorage()
}

class OhosStorage() : IStorage {

    private val mmkvs = mutableMapOf<String, Long>()

    override fun setKV(tableName: String, key: String, value: String) {
        mmkv_c_set_string(getMmKv(tableName), key, value)
    }

    override fun getKV(
        tableName: String,
        key: String,
        defaultValue: String
    ): String {
        return mmkv_c_get_string(getMmKv(tableName), key).usingKString() ?: defaultValue
    }

    override fun removeValue(tableName: String, key: String) {
        mmkv_c_remove_value_for_key(getMmKv(tableName), key)
    }

    override fun getAllKeys(tableName: String): List<String> {
        val keys = mutableListOf<String>()

        mmkv_c_all_keys(getMmKv(tableName)).using { result ->
            val size = result.size
            val items = result.items

            for (i in 0 until size) {
                val keyPtr = items?.get(i)
                if (keyPtr != null) {
                    keys.add(keyPtr.toKString())
                    free(keyPtr)
                }
            }

            free(items)
        }
        return keys
    }

    override fun getAll(tableName: String): Map<String, String> {
        return getAllKeys(tableName).associateWith { getKV(tableName, it) }
    }

    override fun clearKV(tableName: String) {
        mmkv_c_clear_all(getMmKv(tableName))
    }

    private fun getMmKv(tableName: String): Long {
        return mmkvs.getOrPut(tableName) {
            mmkv_c_mmkv_with_id(
                tableName,
                MMKV_C_SINGLE_PROCESS,
                null,
                null,
                0
            )
        }
    }
}