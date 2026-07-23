package com.tencent.news.core.json

import com.tencent.news.core.extension.optBoolean
import com.tencent.news.core.extension.optDouble
import com.tencent.news.core.extension.optFloat
import com.tencent.news.core.extension.optInt
import com.tencent.news.core.extension.optJsonArray
import com.tencent.news.core.extension.optJsonObject
import com.tencent.news.core.extension.optLong
import com.tencent.news.core.extension.optString
import com.tencent.news.core.extension.toJsonArray
import com.tencent.news.core.extension.toJsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

object QuickJson {

    fun String?.toQuickJsonObject(): IQuickJsonObject? = createJsonObject(this)

    fun createJsonObject(json: String?): IQuickJsonObject? {
        val jsonObj = json.toJsonObject() ?: return null
        return KtxQuickJsonObject(jsonObj)
    }

    fun String?.toQuickJsonArray(): IQuickJsonArray? = createJsonArray(this)

    fun createJsonArray(json: String?): IQuickJsonArray? {
        val jsonArray = json.toJsonArray() ?: return null
        return KtxQuickJsonArray(jsonArray)
    }

    // 兼容jsonString和jsonObject
    fun IQuickJsonObject?.compatJsonStringOrObject(key: String): IQuickJsonObject? {
        this ?: return null
        return optObject(key) ?: optString(key).toQuickJsonObject()
    }

    fun IQuickJsonObject?.optStringList(key: String): List<String>? =
        this?.optArray(key)?.map { optString(it) }

    inline fun <T> IQuickJsonArray.map(action: IQuickJsonArray.(index: Int) -> T?): List<T>? {
        val result = mutableListOf<T>()
        for (i in 0 until size) {
            val value = action(i)
            if (value != null) {
                result.add(value)
            }
        }
        return result
    }

    inline fun <T> IQuickJsonArray.optObjectList(action: (json: IQuickJsonObject) -> T?): List<T>? {
        return map { index ->
            val obj = optObject(index)

            if (obj != null) {
                action(obj)
            } else {
                null
            }
        }
    }

}

private class KtxQuickJsonObject(private val jsonObj: JsonObject) : IQuickJsonObject {

    override val keys get() = jsonObj.keys

    override fun optString(key: String): String = jsonObj.optString(key) ?: ""

    override fun optBoolean(key: String): Boolean = jsonObj.optBoolean(key)

    override fun optInt(key: String): Int = jsonObj.optInt(key)

    override fun optLong(key: String): Long = jsonObj.optLong(key)

    override fun optFloat(key: String): Float = jsonObj.optFloat(key)

    override fun optDouble(key: String): Double = jsonObj.optDouble(key)

    override fun optObject(key: String): IQuickJsonObject? {
        val jsonObject = jsonObj.optJsonObject(key) ?: return null
        return KtxQuickJsonObject(jsonObject)
    }

    override fun optArray(key: String): IQuickJsonArray? {
        val jsonArray = jsonObj.optJsonArray(key) ?: return null
        return KtxQuickJsonArray(jsonArray)
    }

    override fun toString(): String = jsonObj.toString()

}

private class KtxQuickJsonArray(private val jsonArray: JsonArray) : IQuickJsonArray {

    override val size: Int get() = jsonArray.size

    override fun optString(index: Int): String = jsonArray.optString(index) ?: ""

    override fun optBoolean(index: Int): Boolean = jsonArray.optBoolean(index)

    override fun optInt(index: Int): Int = jsonArray.optInt(index)

    override fun optLong(index: Int): Long = jsonArray.optLong(index)

    override fun optFloat(index: Int): Float = jsonArray.optFloat(index)

    override fun optDouble(index: Int): Double = jsonArray.optDouble(index)

    override fun optObject(index: Int): IQuickJsonObject? {
        val jsonObject = jsonArray.optJsonObject(index) ?: return null
        return KtxQuickJsonObject(jsonObject)
    }

    override fun optArray(index: Int): IQuickJsonArray? {
        val jsonArray = jsonArray.optJsonArray(index) ?: return null
        return KtxQuickJsonArray(jsonArray)
    }

    override fun toString(): String = jsonArray.toString()

}