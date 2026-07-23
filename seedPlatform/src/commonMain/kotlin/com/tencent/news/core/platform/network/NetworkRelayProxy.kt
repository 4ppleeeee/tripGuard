package com.tencent.news.core.platform.network

import com.tencent.news.core.annotation.TestOnly
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.extension.jsonObj2Map
import com.tencent.news.core.extension.safeDecodeJsonObj
import com.tencent.news.core.platform.PlatformNetworkLog
import com.tencent.news.core.platform.api.INetwork
import com.tencent.news.core.platform.api.INetworkRequest
import com.tencent.news.core.platform.api.INetworkResponse
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.NetworkCallback
import com.tencent.news.core.platform.api.NetworkResponse
import com.tencent.news.core.platform.api.getShiplyStringList
import com.tencent.news.core.platform.getPlatformDate
import com.tencent.news.core.serializer.KtJson
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized


/**
 * 网络请求接力服务，参考：[全场景冷热启动网络请求加速方案](https://doc.weixin.qq.com/doc/w3_AE0AIQaCACcCNZ4JtS1qHQHyDRE5X?scode=AJEAIQdfAAot6u0HTZAE0AIQaCACc)
 *
 * 当满足以下三个条件时，接力自动执行：
 * - [NetworkRelayParams.enabled]为true
 * - [NetworkRelayParams.maxAge] > 0
 * - [NetworkBuilder.cacheRequestKey]不为空
 */
class NetworkRelayProxy(private val target: INetwork) : INetwork by target {

    override fun <T> getRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return NetworkRelayService.sendRequestLocked(builder) {
            target.getRequest(builder)
        }
    }

    override fun <T> jsonPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return NetworkRelayService.sendRequestLocked(builder) {
            target.jsonPostRequest(builder)
        }
    }

    override fun <T> formPostRequest(builder: NetworkBuilder<T>): INetworkRequest {
        return NetworkRelayService.sendRequestLocked(builder) {
            target.formPostRequest(builder)
        }
    }

}

object NetworkRelayService : SynchronizedObject() {

    private val relayingRequests = mutableMapOf<String, NetworkProxy>()

    // 不参与匹配的广告参数集合。
    private val blackAdParamKeys by lazy {
        // 见业务侧AdParam
        val def = listOf<String>(
            "news_scs",             // 包含时间戳、随机值；
            "ext",                  // 包含client_ip、cookie
            "session_id",           // 可能从无到有
            "launchTimestamp",      // 启动时间,小概率会变
            "report_info",          // session时长计算
        )
        getShiplyStringList("network_relay_black_ad_list", def) ?: emptyList()
    }

    fun <T> sendRequestLocked(
        builder: NetworkBuilder<T>,
        execNetRequest: () -> INetworkRequest
    ): INetworkRequest {
        val relayParams = builder.relayParams
        val id = relayParams?.id
        // 不满足接力条件，直接发起请求
        if (relayParams == null || !relayParams.enabled || relayParams.maxAge <= 0 || builder.cacheRequestKey.isNullOrEmpty()) {
            return execNetRequest()
        } else {
            synchronized(this) {
                return sendRequest(builder, execNetRequest)
            }
        }
    }

    private fun <T> sendRequest(
        builder: NetworkBuilder<T>,
        execNetRequest: () -> INetworkRequest
    ): INetworkRequest {
        val relayParams = builder.relayParams
        val id = relayParams?.id
        val cacheKey = builder.getCacheKey()
        var networkProxy = relayingRequests[cacheKey]

        NetworkRelayMonitor.onRelayRequestAdd(builder)

        // 过期则直接移除掉旧请求，重新发起新请求
        if (networkProxy != null && networkProxy.response.isExpired()) {
            relayingRequests.remove(cacheKey)
            NetworkRelayMonitor.onRelayRequestExpired(builder)
            PlatformNetworkLog.fileLog("${id}接力请求已过期：$cacheKey，重新发起新请求")
            networkProxy = null
        }

        // 请求失败则直接移除掉旧请求，重新发起新请求
        if (networkProxy != null && networkProxy.response.isFailed()) {
            relayingRequests.remove(cacheKey)
            NetworkRelayMonitor.onRelayRequestFailed(builder)
            PlatformNetworkLog.fileLog("${id}接力请求失败：$cacheKey，重新发起新请求")
            networkProxy = null
        }

        // 接力未开始：
        if (networkProxy == null) {
            NetworkRelayMonitor.onLeadOffRequestStart(builder)
            // 1. 替换response
            val responseProxy = NetworkRelayResponseProxy(builder, builder.onResponse, cacheKey)
            builder.onResponse = responseProxy
            // 2. 发起网络请求
            PlatformNetworkLog.fileLog("${id}接力尚未开始，替换onResponse为${responseProxy}并发起网络请求：$cacheKey")
            val requestProxy = NetworkRelayRequestProxy(execNetRequest())
            // 3. 记录请求
            relayingRequests[cacheKey] =
                NetworkProxy(requestProxy, responseProxy as NetworkRelayResponseProxy<Any>)
            return requestProxy.request
        } else {
            NetworkRelayMonitor.onRelayRequestHit(builder)
            // 接力已开始，且肯定未过期
            PlatformNetworkLog.fileLog("${id}接力已开始，等待网络响应：$cacheKey")
            networkProxy.response.addRelayReceiver(builder as NetworkBuilder<Any>)
            return networkProxy.request.request
        }
    }

    fun <T> onLastOneReceiverDone(builder: NetworkBuilder<T>?) {
        builder ?: return
        synchronized(this) {
            relayingRequests.remove(builder.getCacheKey())
            NetworkRelayMonitor.onClear(builder)
        }
    }

    fun <T> NetworkBuilder<T>.getCacheKey(): String {
        val bodyKey = this.params?.flatToStableString() ?: "no_body"
        val queryKey = this.forceUrlParams?.flatToStableString() ?: "no_query"
        return "[${this.url}]_[${this.cacheRequestKey}]_[${bodyKey}]_[${queryKey}]"
    }

    private fun Map<String, Any>?.flatToStableString(): String? {
        return this?.entries?.sortedBy { it.key }?.joinToString {
            if (it.key == "adReqData") {
                "${it.key}=${it.value.compatAdReqData()}"
            } else {
                "${it.key}=${it.value}"
            }
        }
    }

    // 兼容广告公参特别容易变动的case
    private fun Any?.compatAdReqData(): Any? {
        val jsonObject = KtJson.safeDecodeJsonObj(this.toString()) ?: return null
        return jsonObject.jsonObj2Map().mapValues { adEntry ->
            if (blackAdParamKeys.contains(adEntry.key)) "ignored_" else adEntry.value
        }
    }

    /**
     * 判断一系列请求是否为一次接力。[INetworkResponse.this]为第一棒，[followers]为后续几棒。
     */
    fun <T> INetworkResponse<T>.isRelay(vararg followers: INetworkResponse<T>): Boolean {
        val leadOff = this.result.relayState ?: return false
        if (leadOff.index != 0) return false
        followers.forEach {
            val followState = it.result.relayState ?: return false
            if (followState.index <= 0 || followState.leadOffId != leadOff.leadOffId) {
                return false
            }
        }
        return true
    }

    @TestOnly
    fun clearCache() = relayingRequests.clear()
}

data class NetworkRelayParams(
    val enabled: Boolean,
    // 缓存有效期，单位ms
    val maxAge: Long,
    val isLastOne: Boolean,
    var id: String? = null
)

/**
 * 网络请求接力状态
 * @param index 接力顺序，从0开始，第一棒为0，后续加1
 * @param leadOffId 接力第一棒的id
 */
data class NetworkRelayState(val index: Int, val leadOffId: String?)

private class NetworkProxy(
    val request: NetworkRelayRequestProxy,
    val response: NetworkRelayResponseProxy<Any>,
)

private class NetworkRelayRequestProxy(val request: INetworkRequest) : INetworkRequest by request

private class NetworkRelayResponseProxy<T>(
    // 接力第一棒
    private val leadOffReceiver: NetworkBuilder<T>,
    // 第一棒的网络响应回调
    private val leadOffOriginResponse: NetworkCallback<T>,
    private val cacheKey: String,
) : NetworkCallback<T> {

    // 其他接力选手
    private val otherReceivers = mutableListOf<NetworkBuilder<T>>()

    // 是否网络请求完成，判断response是否过期
    private var isNetRequestFinished = false

    // 是否网络请求失败
    private var isNetRequestFailed = false
    private var netResponse: INetworkResponse<T>? = null

    // 接收到网络响应的时间戳，单位ms
    private var responseReceivedMilli: Long = Long.MAX_VALUE

    override fun invoke(netResponse: INetworkResponse<T>) = synchronized(NetworkRelayService) {
        this.isNetRequestFinished = true
        this.isNetRequestFailed = !netResponse.result.succeed

        // 接力结束
        this.netResponse = netResponse
        this.responseReceivedMilli = getPlatformDate().getCurTimeMillis()

        // 第一棒请求成功，直接通知；第一棒请求失败，触发后续选手自己的网络请求
        if (!isNetRequestFailed) {
            invokeOnSuccess(netResponse)
        } else {
            invokeOnFailed(netResponse)
        }
    }

    /**
     * 第一棒[leadOffReceiver]的网络响应回调
     */
    private fun invokeOnSuccess(netResponse: INetworkResponse<T>) {

        PlatformNetworkLog.debug { "${leadOffReceiver.id}接力结束，网络请求回来了: ${cacheKey}, 共${otherReceivers.size}个接力选手" }
        // 通知第一棒，网络请求回来了
        val leadOffResponse = NetworkResponse(
            netResponse.json,
            netResponse.result.copy(relayState = NetworkRelayState(0, leadOffReceiver.id)),
            netResponse.parserResult
        )
        leadOffOriginResponse.invoke(leadOffResponse)
        NetworkRelayMonitor.onLeadOffRequestDone(leadOffReceiver, true)

        // 最后一棒是否收到网络响应
        var lastOneReceiver: NetworkBuilder<T>? = null
        if (leadOffReceiver.relayParams?.isLastOne.isTrue()) {
            lastOneReceiver = leadOffReceiver
        }

        // 通知其他接力选手
        this.otherReceivers.forEachIndexed { index, receiver ->

            if (receiver.relayParams?.isLastOne.isTrue()) {
                lastOneReceiver = receiver
            }

            val otherResponse = NetworkResponse(
                netResponse.json,
                netResponse.result.copy(
                    relayState = NetworkRelayState(
                        index + 1,
                        leadOffReceiver.id
                    )
                ),
                // 必须重新解析
                receiver.parser?.onParseJson(netResponse.json.getNonNull())
            )
            receiver.onResponse.invoke(otherResponse)
            NetworkRelayMonitor.onOtherRequestsDone(receiver, true)
        }

        if (lastOneReceiver != null) {
            NetworkRelayService.onLastOneReceiverDone(lastOneReceiver)
        }
    }

    /**
     * 第一棒请求失败，触发后续选手自己的网络请求
     */
    private fun invokeOnFailed(netResponse: INetworkResponse<T>) {

        val leadOffResponse = NetworkResponse(
            netResponse.json,
            netResponse.result.copy(relayState = NetworkRelayState(0, leadOffReceiver.id)),
            netResponse.parserResult
        )
        leadOffOriginResponse.invoke(leadOffResponse)
        // 监控第一棒，网络请求失败
        NetworkRelayMonitor.onLeadOffRequestDone(leadOffReceiver, false)
        // 清除当前请求的所有记录
        NetworkRelayMonitor.onClear(leadOffReceiver)
        // 触发后续选手自己的网络请求
        otherReceivers.forEach {
            it.execute()
        }
    }

    /**
     * 第一棒[leadOffReceiver]发起的请求是否过期
     */
    fun isExpired(): Boolean {
        // 没收到网络响应，则认为尚未过期
        if (!this.isNetRequestFinished) {
            return false
        }
        val maxAge = this.leadOffReceiver.relayParams?.maxAge ?: 0
        return this.responseReceivedMilli + maxAge < getPlatformDate().getCurTimeMillis()
    }

    fun isFailed(): Boolean {
        return this.isNetRequestFailed
    }

    /**
     * 添加接力选手，必须确保第一棒[leadOffReceiver]的请求未过期且未失败
     */
    fun addRelayReceiver(receiver: NetworkBuilder<T>) = synchronized(NetworkRelayService) {

        val netResponse = this.netResponse
        if (netResponse != null && this.isNetRequestFinished) {

            if (receiver.relayParams?.isLastOne.isTrue()) {
                NetworkRelayService.onLastOneReceiverDone(receiver)
            }

            val index = this.otherReceivers.size + 1
            val response = NetworkResponse(
                netResponse.json,
                netResponse.result.copy(relayState = NetworkRelayState(index, leadOffReceiver.id)),
                // 必须重新解析
                receiver.parser?.onParseJson(netResponse.json.getNonNull())
            )
            PlatformNetworkLog.debug { "添加接力选手${receiver.id}时，${leadOffReceiver.id}网络请求已经回来了，直接回调：$cacheKey" }
            receiver.onResponse.invoke(response)
        } else {
            PlatformNetworkLog.debug { "添加接力选手${receiver.id}，等待第一棒的网络响应：$cacheKey" }
            otherReceivers.add(receiver)
        }
    }

    private val NetworkBuilder<T>.id get() = relayParams?.id

}
