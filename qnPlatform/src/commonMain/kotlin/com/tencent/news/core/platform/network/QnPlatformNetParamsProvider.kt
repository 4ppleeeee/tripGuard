package com.tencent.news.core.platform.network

import com.tencent.news.core.platform.ConcurrentMap
import com.tencent.news.core.platform.api.IPlatformNetParamsInjector
import com.tencent.news.core.platform.api.IPlatformNetParamsProvider

val qnHttpParamProvider: IPlatformNetParamsProvider = QnPlatformNetParamsProvider

val qnPlatformNetworkParamsInjector: IPlatformNetParamsInjector = QnPlatformNetParamsProvider

private object QnPlatformNetParamsProvider : IPlatformNetParamsInjector,
    IPlatformNetParamsProvider {

    private val globalHeaders: ConcurrentMap<String, String> = ConcurrentMap()
    private val globalParams: ConcurrentMap<String, Any> = ConcurrentMap()
    private var globalCookie: String = ""
    private var userAgent: String = ""

    override fun injectGlobalHeaders(headers: Map<String, String>) {
        globalHeaders.putAll(headers)
    }

    override fun injectGlobalParams(params: Map<String, Any>) {
        globalParams.putAll(params)
    }

    override fun injectGlobalCookies(cookies: String) {
        globalCookie = cookies
    }

    override fun injectUserAgent(ua: String) {
        userAgent = ua
    }

    override fun getGlobalHeader(key: String): String? {
        return globalHeaders[key]
    }

    override fun getUserAgent(): String {
        return userAgent
    }

    override fun getGlobalParam(key: String): Any? {
        return globalParams[key]
    }

    override fun getGlobalCookies(): String {
        return globalCookie
    }

}
