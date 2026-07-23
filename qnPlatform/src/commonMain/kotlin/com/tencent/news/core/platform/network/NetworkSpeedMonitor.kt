package com.tencent.news.core.platform.network

import kotlinx.coroutines.flow.Flow

/**
 * 网络速度监测器接口
 */
interface NetworkSpeedMonitor {
    /**
     * 获取当前网络速度
     * @return 格式化的网络速度字符串，例如 "1.2MB/s"
     */
    fun getCurrentSpeed(): String
    
    /**
     * 获取网络速度流，可以用于观察网络速度变化
     * @return 网络速度流，包含描述和实际速度值
     */
    fun getSpeedFlow(): Flow<NetworkSpeed>
    
    /**
     * 开始监测网络速度
     */
    fun startMonitoring()
    
    /**
     * 停止监测网络速度
     */
    fun stopMonitoring()
}

/**
 * 网络速度数据类
 * @param desc 格式化的网络速度描述，例如 "1.2MB/s"
 * @param speed 实际的网络速度值，单位为字节/秒
 */
data class NetworkSpeed(val desc: String = "", val speed: Long = 0)