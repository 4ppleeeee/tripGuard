package com.tencent.news.core.platform.network

/**
 * 网络速度监测器工厂类
 */
expect object NetworkSpeedMonitorFactory {
    /**
     * 获取平台特定的网络速度监测器实例
     *
     * @param intervalMillis 监测间隔时间，单位为毫秒，默认为1000毫秒
     * @return 网络速度监测器实例
     */
    fun getNetworkSpeedMonitor(intervalMillis: Long = 1000): NetworkSpeedMonitor
} 