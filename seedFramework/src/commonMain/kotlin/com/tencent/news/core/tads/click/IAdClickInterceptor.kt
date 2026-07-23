package com.tencent.news.core.tads.click

interface IAdClickInterceptor {
    // 返回true表示拦截，不继续执行后续processor
    fun doIntercept(type: AdClickProcessorType): Boolean = false

    fun onSuccess(type: AdClickProcessorType) {}
    fun onFail(type: AdClickProcessorType) {}

    fun interceptType(): AdClickProcessorType
}