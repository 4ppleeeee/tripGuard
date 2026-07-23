package com.tencent.news.core.platform.network

import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.date
import platform.Foundation.stringWithFormat
import platform.Foundation.timeIntervalSince1970

// 定义网速监测器接口，由 iOS 宿主实现
interface NativeNetworkSpeedMonitor {
    fun getTotalRxBytes(): Long?
}

// 由宿主实现和赋值的变量
var nativeMonitor: NativeNetworkSpeedMonitor? = null

class IOSNetworkSpeedMonitor(intervalMillis: Long = 1000) : BaseNetworkSpeedMonitor(intervalMillis) {
    override fun getTotalRxBytes(): Long {
        return nativeMonitor?.getTotalRxBytes() ?: 0
    }
    
    override fun getCurrentTimeMillis(): Long {
        return (NSDate.date().timeIntervalSince1970 * 1000).toLong()
    }
    
    override fun formatDecimal(value: Double): String {
        return NSString.stringWithFormat("%.1f", value)
    }
} 