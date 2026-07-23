package com.tencent.news.core.app.controller

import com.tencent.news.core.list.model.NewsFeedsSLO
import com.tencent.news.core.platform.ConcurrentMap

object AppTmpStorage {

    object Api { // 暴露的jsapi名
        const val SET = "setTmpObj"
        const val GET = "getTmpObj"
        const val REMOVE = "removeTmpObj"
    }

    private val tmpStorage = ConcurrentMap<String, Any>()

    fun setTmpObj(key: String, obj: Any?): Boolean {
        if (!key.isKeyValid()) {
            return false
        }
        if (obj == null) {
            tmpStorage.remove(key)
        } else {
            tmpStorage[key] = obj
        }
        return true
    }

    fun getTmpObj(key: String): Any? {
        if (!key.isKeyValid()) {
            NewsFeedsSLO.debugLog { "getTmpObj 未命中缓存：${key}" }
            return null
        }
        val result = tmpStorage[key]
        if (result != null) {
            NewsFeedsSLO.debugLog { "getTmpObj 命中缓存：${key}" }
        }
        return result
    }

    fun removeTmpObj(key: String): Boolean {
        if (!key.isKeyValid()) {
            return false
        }
        val removed = tmpStorage.remove(key) != null
        return removed
    }

    private fun String.isKeyValid(): Boolean {
        return isNotEmpty()
    }

}