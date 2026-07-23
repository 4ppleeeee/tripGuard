package com.tencent.news.core.platform.api

// 传感器监控轴枚举
// 用于指定需要监听的传感器轴向
enum class SensorAxis {
    X,  // X轴
    Y,  // Y轴
    Z   // Z轴
}

// 传感器配置类
// 用于在注册传感器监听时传入配置参数
// monitorAxes: 需要监控的轴列表，默认监控所有轴。iOS 平台可指定只监听特定轴
// samplingRate: 采样率，控制传感器数据回调频率
// initialAngle: 初始角度（度数），iOS 平台需要设置初始参考角度，Android 平台会忽略此参数
// callbackIntervalMs: 回调间隔（毫秒），iOS 平台用于控制多少毫秒回调一次，Android 通过 samplingRate 控制，会忽略此参数
data class SensorConfig(
    val monitorAxes: Set<SensorAxis> = setOf(SensorAxis.X, SensorAxis.Y, SensorAxis.Z),
    val samplingRate: SensorSamplingRate = SensorSamplingRate.UI,
    val initialAngle: Float = 0f,
    val callbackIntervalMs: Long = 0L
) {
    companion object {
        // 默认配置：监控所有轴，UI 采样率
        val DEFAULT = SensorConfig()

        // 仅监控 Y 轴，用于扭动互动场景
        val TWIST_INTERACT = SensorConfig(
            monitorAxes = setOf(SensorAxis.Y),
            samplingRate = SensorSamplingRate.UI
        )
    }
}
