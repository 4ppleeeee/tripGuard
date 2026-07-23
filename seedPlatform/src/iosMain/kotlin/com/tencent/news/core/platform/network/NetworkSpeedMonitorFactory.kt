package com.tencent.news.core.platform.network

actual object NetworkSpeedMonitorFactory {
    actual fun getNetworkSpeedMonitor(intervalMillis: Long): NetworkSpeedMonitor {
        return IOSNetworkSpeedMonitor(intervalMillis)
    }
} 