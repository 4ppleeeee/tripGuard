package com.tencent.news.core.platform

interface ISystemVolumeController {
    fun registerListener(listener: IVolumeListener)
    fun unregisterListener(listener: IVolumeListener)
    fun getVolumeRate(): Float = 0f
    fun setVolumeRate(rate: Float) {}
}

fun appVolumeController() = QnPlatformLogic.systemVolumeController

fun interface IVolumeListener {
    fun onVolumeChanged(volume: Int, isUp: Boolean)
}
