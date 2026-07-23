package com.tencent.news.core.platform.network

import com.tencent.kuikly.core.datetime.DateTime
import com.tencent.news.core.ohos.setup.knoi.consumer.ohosNetworkService
import com.tencent.tmm.knoi.type.asPromise

/**
 * 鸿蒙平台的网络速度监控实现
 */
class HarmonyNetworkSpeedMonitor(intervalMillis: Long = 1000) : BaseNetworkSpeedMonitor(intervalMillis) {
    private var cachedRxBytes = 0L
    override fun getTotalRxBytes(): Long {
        ohosNetworkService.getTotalRxBytes().asPromise().then { data ->
            if (data.isNotEmpty() && data[0].isNumber()) {
                cachedRxBytes = data[0].toLong()
            }
        }
        return cachedRxBytes
    }

    override fun getCurrentTimeMillis(): Long {
        return DateTime.currentTimestamp() * 1000
    }

    /**
     * 格式化小数，保留一位小数
     */
    override fun formatDecimal(value: Double): String {
        // 鸿蒙平台无法使用String.format方法，只能使用字符串拼接
        val speedStr = (value * 10).toInt().toString()
        return "${speedStr.dropLast(1)}.${speedStr.takeLast(1)}"
    }
}