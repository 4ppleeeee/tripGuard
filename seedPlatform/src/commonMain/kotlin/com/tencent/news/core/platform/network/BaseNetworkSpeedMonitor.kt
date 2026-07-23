package com.tencent.news.core.platform.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

/**
 * 网络速度监控基类，处理共享的监控逻辑
 */
abstract class BaseNetworkSpeedMonitor(private val intervalMillis: Long = 1000) : NetworkSpeedMonitor {
    private val speedFlow = MutableStateFlow(NetworkSpeed("0 KB/s", 0))
    private var isMonitoring = false
    private val monitorScope = CoroutineScope(Dispatchers.Default)
    
    private var lastTotalRxBytes: Long = 0
    private var lastUpdateTime: Long = 0
    
    override fun getCurrentSpeed(): String {
        return speedFlow.value.desc
    }
    
    override fun getSpeedFlow(): Flow<NetworkSpeed> {
        return speedFlow
    }

    override fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        
        // 初始化监控
        lastTotalRxBytes = getTotalRxBytes()
        lastUpdateTime = getCurrentTimeMillis()
        
        monitorScope.launch {
            try {
                while (isMonitoring) {
                    delay(intervalMillis)
                    if (isMonitoring) {
                        try {
                            updateNetworkSpeed()
                        } catch (e: Exception) {
                            // 记录错误但不中断监控循环
                        }
                    }
                }
            } catch (e: CancellationException) {
                // 协程被取消，正常情况，不需要特殊处理
            } catch (e: Exception) {
                // 处理其他异常
                isMonitoring = false
            }
        }
    }
    
    override fun stopMonitoring() {
        isMonitoring = false
    }
    
    /**
     * 获取当前总接收字节数
     */
    protected abstract fun getTotalRxBytes(): Long
    
    /**
     * 获取当前时间（毫秒）
     */
    protected abstract fun getCurrentTimeMillis(): Long
    
    /**
     * 更新网络速度
     */
    private fun updateNetworkSpeed() {
        val currentRxBytes = getTotalRxBytes()
        val currentTime = getCurrentTimeMillis()
        
        val timeDifference = currentTime - lastUpdateTime
        if (timeDifference <= 0) return
        
        // 计算下载速度
        val speedInBytes = calculateSpeed(currentRxBytes - lastTotalRxBytes, timeDifference)
        
        // 格式化网络速度
        val formattedSpeed = formatSpeed(speedInBytes)
        
        // 更新状态
        speedFlow.value = NetworkSpeed(formattedSpeed, speedInBytes)
        
        // 更新上次的值
        lastTotalRxBytes = currentRxBytes
        lastUpdateTime = currentTime
    }
    
    /**
     * 计算速度
     */
    private fun calculateSpeed(bytesDifference: Long, timeDifferenceMs: Long): Long {
        return (bytesDifference * 1000) / timeDifferenceMs
    }
    
    /**
     * 格式化网络速度
     */
    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 0 -> "0 B/s"
            bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
            bytesPerSecond < 1024 * 1024 -> {
                val speed = bytesPerSecond / 1024.0
                formatDecimal(speed) + " KB/s"
            }
            bytesPerSecond < 1024 * 1024 * 1024 -> {
                val speed = bytesPerSecond / (1024.0 * 1024.0)
                formatDecimal(speed) + " MB/s"
            }
            else -> {
                val speed = bytesPerSecond / (1024.0 * 1024.0 * 1024.0)
                formatDecimal(speed) + " GB/s"
            }
        }
    }
    
    /**
     * 格式化小数，保留一位小数
     */
    protected abstract fun formatDecimal(value: Double): String
} 