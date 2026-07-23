package com.tencent.news.core.json

interface IQuickJsonEncoder<T> {
    fun quickEncodeToJson(obj: T?): String?
}

// 大多数model只需要实现解析即可
interface IQuickJsonDecoder<T> {
    fun quickDecodeFromJson(json: IQuickJsonObject): T?
}

// 同时需要解析+序列化时，实现这个
interface IQuickJsonSerializer<T> : IQuickJsonEncoder<T>, IQuickJsonDecoder<T>

interface IQuickJsonObject {
    val keys: Set<String>

    fun optString(key: String): String
    fun optBoolean(key: String): Boolean
    fun optInt(key: String): Int
    fun optLong(key: String): Long
    fun optFloat(key: String): Float
    fun optDouble(key: String): Double
    fun optObject(key: String): IQuickJsonObject?
    fun optArray(key: String): IQuickJsonArray?

    override fun toString(): String
}

interface IQuickJsonArray {
    val size: Int

    fun optString(index: Int): String
    fun optBoolean(index: Int): Boolean
    fun optInt(index: Int): Int
    fun optLong(index: Int): Long
    fun optFloat(index: Int): Float
    fun optDouble(index: Int): Double
    fun optObject(index: Int): IQuickJsonObject?
    fun optArray(index: Int): IQuickJsonArray?

    override fun toString(): String
}