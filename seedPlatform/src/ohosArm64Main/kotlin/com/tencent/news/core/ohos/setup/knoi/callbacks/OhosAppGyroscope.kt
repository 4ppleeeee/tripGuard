package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.GyroscopeData
import com.tencent.news.core.platform.api.IAppGyroscope
import com.tencent.news.core.platform.api.IGyroscopeListener
import com.tencent.news.core.platform.api.SensorAccuracy
import com.tencent.news.core.platform.api.SensorAxis
import com.tencent.news.core.platform.api.SensorConfig
import com.tencent.news.core.platform.api.SensorSamplingRate
import com.tencent.news.core.platform.qnLogcat
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppGyroscope = JSValue

/**
 * 注入鸿蒙端 [IAppGyroscope] 实现。
 *
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧基于 @kit.SensorServiceKit 的 sensor API 真实实现
 * 桥接到 KMP 层：
 *  - ArkTS 侧只需提供【启停 + 数据回调 + 可用性查询】，复杂度最低。
 *  - 多 listener 管理、monitorAxes 过滤、latest 缓存等逻辑全部在 Kotlin 侧完成，与
 *    Android/iOS 实现对齐。
 *
 * ArkTS 侧通过 Kommon.setup 调用 getHarmonyStartupProvider().setupAppGyroscope(new
 * OhosAppGyroscopeCallback()) 注入实现。
 */
fun setupOhosAppGyroscope(gyroscope: IOhosAppGyroscope) {
    QnPlatformLogic.gyroscope = OhosAppGyroscopeProvider(gyroscope.asOhosAppGyroscope())
}

/**
 * Kotlin 侧的 [IAppGyroscope] 实现，负责：
 *  1. 维护业务 listener 集合（支持多订阅者，与 Android/iOS 语义一致）
 *  2. 首个 listener 注册时让 ArkTS 侧真正调用 sensor.on；最后一个取消时调用 sensor.off
 *  3. 从 ArkTS 回传的 x/y/z/timestamp 组装 [GyroscopeData]，并按 [SensorConfig.monitorAxes] 过滤
 *  4. 缓存 latestData 供 [getLatestData] 同步查询
 */
private class OhosAppGyroscopeProvider(
    private val native: OhosAppGyroscope,
) : IAppGyroscope {

    private companion object {
        const val TAG = "OhosAppGyroscope"
    }

    // 业务 listener 与对应的配置（用于 monitorAxes 过滤）
    private val listeners = mutableMapOf<IGyroscopeListener, SensorConfig>()

    // ArkTS 侧 sensor 注册状态，幂等保护
    private var isNativeRegistered = false

    // ArkTS 侧最近一次回调上来的采样率，用于记录（ArkTS 侧真正决定 sensor.on 的 interval）
    private var currentSamplingRate: SensorSamplingRate = SensorSamplingRate.UI

    // 最新一帧陀螺仪数据
    private var latestData: GyroscopeData? = null

    override fun isAvailable(): Boolean {
        return runCatching { native.isAvailable() }
            .onFailure { qnLogcat()?.logE(TAG, "isAvailable failed", it) }
            .getOrDefault(false)
    }

    override fun registerListener(config: SensorConfig, listener: IGyroscopeListener): Boolean {
        if (!isAvailable()) {
            qnLogcat()?.logI(TAG, "registerListener aborted: sensor unavailable")
            return false
        }
        // 幂等：同一 listener 重复注册只更新 config
        listeners[listener] = config
        if (isNativeRegistered) {
            // 已经在监听，新 listener 直接收后续数据即可
            return true
        }
        // 首个 listener 注册：选择一个合理的采样率给 ArkTS 侧（以第一个 listener 的配置为准）
        currentSamplingRate = config.samplingRate
        return runCatching {
            native.startListening(config.samplingRate.name) { x, y, z, timestamp ->
                onNativeGyroscopeData(x, y, z, timestamp)
            }
            isNativeRegistered = true
            true
        }.onFailure {
            qnLogcat()?.logE(TAG, "startListening failed", it)
            // 注册失败时回滚，避免业务以为注册成功
            listeners.remove(listener)
        }.getOrDefault(false)
    }

    override fun unregisterListener(listener: IGyroscopeListener) {
        if (listeners.remove(listener) == null) return
        if (listeners.isEmpty()) {
            stopNativeListening()
        }
    }

    override fun unregisterAllListeners() {
        if (listeners.isEmpty()) return
        listeners.clear()
        stopNativeListening()
    }

    override fun getLatestData(): GyroscopeData? = latestData

    // region 内部实现

    private fun stopNativeListening() {
        if (!isNativeRegistered) return
        isNativeRegistered = false
        runCatching { native.stopListening() }
            .onFailure { qnLogcat()?.logE(TAG, "stopListening failed", it) }
    }

    /**
     * 处理 ArkTS 侧回调上来的原始陀螺仪数据。
     *
     * @param x X轴角速度 (rad/s)
     * @param y Y轴角速度 (rad/s)
     * @param z Z轴角速度 (rad/s)
     * @param timestampNanos 纳秒级时间戳（ArkTS 侧需传 SensorResponse.timestamp，本身即纳秒）
     */
    private fun onNativeGyroscopeData(x: Double, y: Double, z: Double, timestampNanos: Double) {
        val data = GyroscopeData(
            x = x.toFloat(),
            y = y.toFloat(),
            z = z.toFloat(),
            timestamp = timestampNanos.toLong(),
            accuracy = SensorAccuracy.UNKNOWN, // ArkTS sensor API 不提供精度信息
        )
        latestData = data
        // 遍历 listener，按各自的 monitorAxes 过滤数据
        // 注意：toList() 做一次快照，避免回调里 unregister 造成 ConcurrentModificationException
        listeners.toList().forEach { (listener, config) ->
            val filtered = data.filterByAxes(config.monitorAxes)
            listener.onGyroscopeChanged(filtered)
        }
    }

    /**
     * 按监控轴过滤：未监控的轴置 0f。与 iOS 的 monitorAxes 语义保持一致。
     * 若监控所有轴（默认），直接返回原始数据。
     */
    private fun GyroscopeData.filterByAxes(monitorAxes: Set<SensorAxis>): GyroscopeData {
        if (monitorAxes.size == 3) return this
        return copy(
            x = if (SensorAxis.X in monitorAxes) x else 0f,
            y = if (SensorAxis.Y in monitorAxes) y else 0f,
            z = if (SensorAxis.Z in monitorAxes) z else 0f,
        )
    }

    // endregion
}

/**
 * ArkTS 侧陀螺仪能力实现接口。
 *
 * knoi 编译时会自动生成 ArkTS 侧的接口定义，ArkTS 侧 OhosAppGyroscopeCallback 实现该接口
 * 并通过 getHarmonyStartupProvider().setupAppGyroscope 注入。
 *
 * 设计要点：
 *  - 异步数据通过 [startListening] 的 trailing lambda 持续回调（与 OhosAppStatus.subscribeTheme 风格一致）
 *  - 仅传递原始 x/y/z/timestamp，数据转换、轴过滤、多 listener 分发在 Kotlin 侧完成
 */
@KNCallback
interface OhosAppGyroscope {

    /**
     * 判断设备是否支持陀螺仪传感器。
     * ArkTS 侧可通过 sensor.getSingleSensor(SensorId.GYROSCOPE) 判断，
     * 未找到则返回 false。
     */
    fun isAvailable(): Boolean

    /**
     * 启动陀螺仪监听。
     *
     * @param samplingRateName 采样率名称（NORMAL/UI/GAME/FASTEST，对应
     *                         [SensorSamplingRate.name]），ArkTS 侧映射到
     *                         sensor.SensorFrequency 或 interval 毫秒。
     * @param onData 每次传感器回调时调用，参数为 (x, y, z, timestampNanos)。
     *               timestampNanos 为 SensorResponse.timestamp（鸿蒙侧即纳秒级）。
     */
    fun startListening(
        samplingRateName: String,
        onData: (x: Double, y: Double, z: Double, timestampNanos: Double) -> Unit,
    )

    /**
     * 停止陀螺仪监听，对应 ArkTS 的 sensor.off(SensorId.GYROSCOPE)。
     */
    fun stopListening()
}
