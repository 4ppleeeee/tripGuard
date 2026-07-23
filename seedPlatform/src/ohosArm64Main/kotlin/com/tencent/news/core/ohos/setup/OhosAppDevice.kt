package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppDevice
import com.tencent.news.core.platform.api.IHarmonyDevice

/**
 * 鸿蒙端 appDevice 注入。
 * 将 IAppDevice 的鸿蒙实现注入到 QnPlatformLogic，使 appDevice().isHarmony() 返回 true。
 */
fun setupOhosAppDevice() {
    QnPlatformLogic.appDevice = OhosAppDeviceProvider()
}

private class OhosAppDeviceProvider : IAppDevice {
    private val harmonyDevice = OhosHarmonyDevice()

    override fun getHarmonyRom(): IHarmonyDevice = harmonyDevice
}

private class OhosHarmonyDevice : IHarmonyDevice
