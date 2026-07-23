package com.tencent.news.core.platform.network

import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.extension.toIntString
import com.tencent.news.core.list.trace.NetworkLog
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.platform.api.appReport
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.getCurTimeMillis
import com.tencent.news.core.platform.network.NetworkRelayService.getCacheKey
import com.tencent.news.core.serializer.KtJson

internal object NetworkRelayMonitor {

    private val relayingRequests = mutableMapOf<String, MutableList<NetworkBuilderInfo>>()

    fun <T> onRelayRequestAdd(builder: NetworkBuilder<T>) {
        val stableCacheKey = builder.stableCacheKey()
        if (!relayingRequests.containsKey(stableCacheKey)) {
            relayingRequests[stableCacheKey] = mutableListOf()
        }
        val list = relayingRequests[stableCacheKey]
        list?.add(
            NetworkBuilderInfo(
                startMillis = currentMillis(),
                builder = builder as NetworkBuilder<Any>
            )
        )
    }

    fun <T> onLeadOffRequestDone(builder: NetworkBuilder<T>, isSucceed: Boolean) {
        reportCost(isLeadOff = true, builder = builder, isSucceed = isSucceed)
    }

    fun <T> onOtherRequestsDone(builder: NetworkBuilder<T>, isSucceed: Boolean) {
        reportCost(isLeadOff = false, builder = builder, isSucceed = isSucceed)
    }

    fun <T> onRelayRequestExpired(builder: NetworkBuilder<T>) {
        builder.findInfo()?.isLeadOff4Expired = true
    }

    fun <T> onRelayRequestFailed(builder: NetworkBuilder<T>) {
        builder.findInfo()?.isLeadOff4Failed = true
    }

    fun <T> onRelayRequestHit(builder: NetworkBuilder<T>) {
        builder.findInfo()?.isHit = true
    }

    fun <T> onLeadOffRequestStart(builder: NetworkBuilder<T>) {
        if (isLeadOffBecauseMissRelay(builder)) {
            builder.findInfo()?.isHit = false
            builder.reportMissToBugly()
            debugToast("【错误 ❌】 ${builder.stableCacheKey()}未命中请求接力，请查看 NetRelay 日志")
        }
    }

    fun <T> onClear(builder: NetworkBuilder<T>) {
        relayingRequests.remove(builder.stableCacheKey())
    }

    /**
     * 判断是否因为未命中接力导致成为第一棒
     */
    private fun <T> isLeadOffBecauseMissRelay(builder: NetworkBuilder<T>): Boolean {
        val list = relayingRequests[builder.stableCacheKey()] ?: return false
        val cacheKey = builder.getCacheKey()
        // 大概率都不会miss，所以先简单判断下，提高性能
        if (list.size < 2) return false
        // 如果同一个cgi下有2个以上参数不同的请求，则认为未命中缓存
        return list.filter { it.builder.getCacheKey() != cacheKey }.size >= 2
    }

    /**
     * 上报接口耗时
     */
    private fun <T> reportCost(isLeadOff: Boolean, builder: NetworkBuilder<T>, isSucceed: Boolean) {
        val info = builder.findInfo()
        if (info != null) {
            val currentMillis = currentMillis()
            val cost = currentMillis - info.startMillis
            val savedCost =
                currentMillis - (builder.findLeadOff()?.startMillis ?: info.startMillis) - cost
            val shouldCheckMissReason = !isLeadOff && !info.isHit
            var missReason: String = ""
            if (shouldCheckMissReason) {
                missReason = builder.getMissReason()
            }
            val params = mapOf(
                "url" to builder.url,
                "stableCacheKey" to builder.stableCacheKey(),
                "isLeadOff" to isLeadOff.toIntString(),
                // 当次请求因为过期未命中接力
                "isLeadOff4Expired" to info.isLeadOff4Expired.toIntString(),
                // 当次请求是否失败
                "isLeadOff4Failed" to info.isLeadOff4Failed.toIntString(),
                // 当前请求缓存有效期
                "maxAge" to builder.relayParams?.maxAge.toString(),
                // 当次请求是否是最后一棒(即非preload请求)
                "isLastOne" to builder.relayParams?.isLastOne.toIntString(),
                // 当次请求是否命中接力，第一棒
                "isHit" to info.isHit.toIntString(),
                "missReason" to missReason,
                // 当次请求的耗时（如果是命中接力，则为发起到第一棒结束的时间
                "cost" to "$cost",
                // 节省了多少毫秒
                "savedCost" to "$savedCost",
                // 当次请求是否成功
                "isSucceed" to isSucceed.toIntString(),
            )
            NetworkLog.debug { "${builder.stableCacheKey()}接力结果：${KtJson.safeEncode(params)}" }
            appReport().reportBeacon(event = "network_relay_monitor", params = params)
        } else {
            NetworkLog.error("请求结束后未查找到对应的BuilderInfo")
        }
    }

    private fun <T> NetworkBuilder<T>.getMissReason(): String {
        val list = relayingRequests[this.stableCacheKey()] ?: return "list is empty"
        return list.joinToString(separator = "---\n") { it.builder.getCacheKey() }
    }

    private fun <T> NetworkBuilder<T>.findInfo(): NetworkBuilderInfo? {
        return relayingRequests[stableCacheKey()]?.firstOrNull { it.builder == this }
    }

    private fun <T> NetworkBuilder<T>.findLeadOff(): NetworkBuilderInfo? {
        return relayingRequests[stableCacheKey()]?.firstOrNull()
    }

    private fun <T> NetworkBuilder<T>.reportMissToBugly() {
        val error = "Network Relay Missed: ${this.stableCacheKey()}, \n ${getMissReason()}"
        appReport().reportBugly(
            msg = "Network Relay Missed: ${this.stableCacheKey()}",
            error = NetworkRelayIllegalStateException(error)
        )
    }

    private fun <T> NetworkBuilder<T>.stableCacheKey(): String {
        // 取url和cacheRequestKey
        return "${this.url}_${this.cacheRequestKey}"
    }

    private fun currentMillis(): Long = getCurTimeMillis()
}

private data class NetworkBuilderInfo(
    // 请求开始时间
    val startMillis: Long,
    val builder: NetworkBuilder<Any>,
    // 当次请求因为过期未命中接力
    var isLeadOff4Expired: Boolean = false,
    // 当次请求因为第一棒请求失败未命中接力
    var isLeadOff4Failed: Boolean = false,
    // 当次请求是否命中接力，第一棒必定为false，其他为true
    var isHit: Boolean = false,
)

private class NetworkRelayIllegalStateException(msg: String) : IllegalStateException(msg)