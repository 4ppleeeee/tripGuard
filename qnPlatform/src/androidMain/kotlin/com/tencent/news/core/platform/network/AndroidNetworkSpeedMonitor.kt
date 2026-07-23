package com.tencent.news.core.platform.network

import android.net.TrafficStats

class AndroidNetworkSpeedMonitor(private val intervalMillis: Long = 1000) : BaseNetworkSpeedMonitor(intervalMillis) {
    override fun getTotalRxBytes(): Long {
        return TrafficStats.getTotalRxBytes()
    }

    override fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun formatDecimal(value: Double): String {
        return String.format("%.1f", value)
    }
}