package com.tencent.news.core.platform.api

// 陀螺仪传感器数据监听器
// 各端实现时需将平台原生传感器数据转换为 GyroscopeData
fun interface IGyroscopeListener {
    // 传感器数据更新回调
    fun onGyroscopeChanged(data: GyroscopeData)
}

// 传感器精度变化监听器（可选）
fun interface ISensorAccuracyListener {
    // 传感器精度变化回调
    fun onAccuracyChanged(accuracy: SensorAccuracy)
}
