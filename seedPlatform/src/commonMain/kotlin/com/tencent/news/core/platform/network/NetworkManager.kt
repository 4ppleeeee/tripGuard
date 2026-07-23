package com.tencent.news.core.platform.network

import com.tencent.news.core.extension.ResultCodeEx
import com.tencent.news.core.extension.ResultEx
import com.tencent.news.core.platform.api.DefaultNetworkRequest
import com.tencent.news.core.platform.api.INetwork
import com.tencent.news.core.platform.api.INetworkRequest
import com.tencent.news.core.platform.api.IPlatformNetParamsInjector
import com.tencent.news.core.platform.api.IPlatformNetParamsProvider
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.NetworkResponse
import com.tencent.news.core.platform.api.SameImplNetwork

/**
 * 网络模块全局管理器。
 *
 * 使用前需要通过 [init] 注入 [INetwork] 实现。
 */
object NetworkManager {

    private var _network: INetwork? = null
    private var _paramsInjector: IPlatformNetParamsInjector? = null
    private var _paramsProvider: IPlatformNetParamsProvider? = null

    val network: INetwork
        get() = _network ?: errorNetwork

    val paramsProvider: IPlatformNetParamsProvider?
        get() = _paramsProvider

    val paramsInjector: IPlatformNetParamsInjector?
        get() = _paramsInjector

    fun init(
        network: INetwork,
        paramsInjector: IPlatformNetParamsInjector? = null,
        paramsProvider: IPlatformNetParamsProvider? = null,
    ) {
        _network = network
        _paramsInjector = paramsInjector
        _paramsProvider = paramsProvider
    }

    fun getGlobalHeaders(): Map<String, String> {
        val provider = _paramsProvider ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        val ua = provider.getUserAgent()
        if (ua.isNotEmpty()) {
            result["User-Agent"] = ua
        }
        val cookies = provider.getGlobalCookies()
        if (cookies.isNotEmpty()) {
            result["Cookie"] = cookies
        }
        return result
    }

    fun getGlobalParams(): Map<String, Any> {
        return emptyMap()
    }
}

private val errorNetwork by lazy { ErrorNetwork() }

private class ErrorNetwork : SameImplNetwork() {
    override fun <T> defaultRequestImpl(builder: NetworkBuilder<T>): INetworkRequest {
        builder.onResponse.invoke(
            NetworkResponse(
                json = "",
                result = ResultEx(
                    succeed = false,
                    msg = "NetworkManager 未初始化，请先调用 NetworkManager.init()",
                    errorCode = ResultCodeEx.ERROR
                ),
                parserResult = null
            )
        )
        return DefaultNetworkRequest()
    }

    override fun netState(): NetState = NetState.WIFI
}
