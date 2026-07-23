package com.tencent.news.core.platform.api

fun interface DynamicParamsProvider {
    fun invoke(): Map<String, Any>?
}