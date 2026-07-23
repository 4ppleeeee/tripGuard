package com.tencent.news.core.platform.api

// 陀螺仪数据
data class GyroscopeData(
    val x: Float,       // X轴角速度 (rad/s)
    val y: Float,       // Y轴角速度 (rad/s)
    val z: Float,       // Z轴角速度 (rad/s)
    val timestamp: Long = 0L,  // 采样时间戳（纳秒），平台实现必须传入 System.nanoTime() 或等效纳秒值
    val accuracy: SensorAccuracy = SensorAccuracy.UNKNOWN
) {
    companion object {
        val EMPTY = GyroscopeData(0f, 0f, 0f)
    }
}

// 传感器精度
enum class SensorAccuracy {
    UNKNOWN, LOW, MEDIUM, HIGH, UNRELIABLE
}

// 传感器采样率
enum class SensorSamplingRate {
    NORMAL, UI, GAME, FASTEST
}
