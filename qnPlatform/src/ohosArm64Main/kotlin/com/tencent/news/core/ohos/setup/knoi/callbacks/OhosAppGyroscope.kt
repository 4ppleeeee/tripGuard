package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.platform.Lock
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.GyroscopeData
import com.tencent.news.core.platform.api.IAppGyroscope
import com.tencent.news.core.platform.api.IGyroscopeListener
import com.tencent.news.core.platform.api.SensorAccuracy
import com.tencent.news.core.platform.api.SensorAxis
import com.tencent.news.core.platform.api.SensorConfig
import com.tencent.news.core.platform.api.SensorSamplingRate
import com.tencent.news.core.platform.synchronized
import com.tencent.news.core.serializer.KtJson
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue
import kotlinx.serialization.Serializable

typealias IOhosAppGyroscope = JSValue

fun setupOhosGyroscope(gyroscope: IOhosAppGyroscope) {
    QnPlatformLogic.gyroscope = OhosAppGyroscopeProvider(gyroscope.asOhosAppGyroscope())
}

private const val LISTENER_ID_PREFIX = "ohos_gyroscope_"

private class OhosAppGyroscopeProvider(
    private val ohosGyroscope: OhosAppGyroscope
) : IAppGyroscope {
    private val lock = Lock()
    private val listenerIds = mutableMapOf<IGyroscopeListener, String>()
    private var listenerSequence = 0
    private var latestData: GyroscopeData? = null

    override fun isAvailable(): Boolean = ohosGyroscope.isAvailable()

    override fun registerListener(config: SensorConfig, listener: IGyroscopeListener): Boolean {
        unregisterListener(listener)
        val listenerId = nextListenerId()
        val succeed = ohosGyroscope.registerListener(
            configJson = KtJson.safeEncode(config.toOhosSensorConfigDto()),
            listenerId = listenerId,
            onGyroscopeChanged = onChanged@{ jsValueArray ->
                if (!isListenerActive(listener, listenerId)) {
                    return@onChanged
                }
                val dataJson = jsValueArray.firstOrNull()?.toKString() ?: return@onChanged
                val data = dataJson.toGyroscopeData() ?: return@onChanged
                latestData = data
                listener.onGyroscopeChanged(data)
            },
        )
        if (succeed) {
            synchronized(lock) {
                listenerIds[listener] = listenerId
            }
        }
        return succeed
    }

    override fun unregisterListener(listener: IGyroscopeListener) {
        val listenerId = synchronized(lock) {
            listenerIds.remove(listener)
        } ?: return
        ohosGyroscope.unregisterListener(listenerId)
        if (synchronized(lock) { listenerIds.isEmpty() }) {
            latestData = null
        }
    }

    override fun unregisterAllListeners() {
        synchronized(lock) {
            listenerIds.clear()
        }
        latestData = null
        ohosGyroscope.unregisterAllListeners()
    }

    override fun getLatestData(): GyroscopeData? {
        return ohosGyroscope.getLatestData().toGyroscopeData() ?: latestData
    }

    private fun nextListenerId(): String = synchronized(lock) {
        listenerSequence += 1
        "$LISTENER_ID_PREFIX$listenerSequence"
    }

    private fun isListenerActive(listener: IGyroscopeListener, listenerId: String): Boolean {
        return synchronized(lock) {
            listenerIds[listener] == listenerId
        }
    }
}

@KNCallback
interface OhosAppGyroscope {
    fun isAvailable(): Boolean

    fun registerListener(
        configJson: String,
        listenerId: String,
        onGyroscopeChanged: (Array<JSValue?>) -> Unit,
    ): Boolean

    fun unregisterListener(listenerId: String)

    fun unregisterAllListeners()

    fun getLatestData(): String
}

@Serializable
private data class OhosSensorConfigDto(
    val monitorAxes: List<String> = listOf(SensorAxis.X.name, SensorAxis.Y.name, SensorAxis.Z.name),
    val samplingRate: String = SensorSamplingRate.UI.name,
    val initialAngle: Float = 0f,
    val callbackIntervalMs: Long = 0L,
)

@Serializable
private data class OhosGyroscopeDataDto(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val timestamp: Long = 0L,
    val accuracy: String = SensorAccuracy.UNKNOWN.name,
)

private fun SensorConfig.toOhosSensorConfigDto(): OhosSensorConfigDto =
    OhosSensorConfigDto(
        monitorAxes = monitorAxes.map { it.name },
        samplingRate = samplingRate.name,
        initialAngle = initialAngle,
        callbackIntervalMs = callbackIntervalMs,
    )

private fun String?.toGyroscopeData(): GyroscopeData? {
    val dto = KtJson.safeDecode<OhosGyroscopeDataDto>(this) ?: return null
    return GyroscopeData(
        x = dto.x,
        y = dto.y,
        z = dto.z,
        timestamp = dto.timestamp,
        accuracy = dto.accuracy.toSensorAccuracy(),
    )
}

private fun String.toSensorAccuracy(): SensorAccuracy =
    SensorAccuracy.values().firstOrNull { it.name == this } ?: SensorAccuracy.UNKNOWN
