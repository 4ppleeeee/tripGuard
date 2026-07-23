package com.tencent.news.core.platform.network

/**
 * 鸿蒙平台的网络速度监控工厂
 */
actual object NetworkSpeedMonitorFactory {
    actual fun getNetworkSpeedMonitor(intervalMillis: Long): NetworkSpeedMonitor {
        return HarmonyNetworkSpeedMonitor(intervalMillis)
    }
}