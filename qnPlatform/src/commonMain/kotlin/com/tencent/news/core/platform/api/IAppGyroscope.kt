package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic

val appGyroscope: IAppGyroscope by lazy { AppGyroscopeInterceptor(QnPlatformLogic.gyroscope) }

interface IAppGyroscope {
    fun isAvailable(): Boolean
    fun registerListener(config: SensorConfig, listener: IGyroscopeListener): Boolean
    fun unregisterListener(listener: IGyroscopeListener)
    fun unregisterAllListeners()
    fun getLatestData(): GyroscopeData?
}

private class AppGyroscopeInterceptor(
    private val platformGyroscope: IAppGyroscope?
) : IAppGyroscope {

    override fun isAvailable(): Boolean = platformGyroscope?.isAvailable() ?: false

    override fun registerListener(config: SensorConfig, listener: IGyroscopeListener): Boolean {
        return platformGyroscope?.registerListener(config, listener) ?: false
    }

    override fun unregisterListener(listener: IGyroscopeListener) {
        platformGyroscope?.unregisterListener(listener)
    }

    override fun unregisterAllListeners() {
        platformGyroscope?.unregisterAllListeners()
    }

    override fun getLatestData(): GyroscopeData? = platformGyroscope?.getLatestData()
}
