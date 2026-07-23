package com.tencent.news.core.platform

interface ISystemBrightnessController {
    fun getBrightness(): Float
    fun setBrightness(value: Float)
    fun reset()
}

fun appBrightnessController() = QnPlatformLogic.systemBrightnessController
