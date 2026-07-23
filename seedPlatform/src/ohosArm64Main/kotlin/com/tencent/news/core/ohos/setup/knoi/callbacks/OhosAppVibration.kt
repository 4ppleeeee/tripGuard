package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppVibration
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readValue
import platform.devices.OH_Vibrator_PlayVibration
import platform.devices.VIBRATOR_USAGE_UNKNOWN
import platform.devices.Vibrator_Attribute

/**
 * 鸿蒙端 appVibration 注入。
 * 直接调用 Harmony C API，避免触发振动时跨 runtime 回调 ArkTS。
 */
fun setupOhosAppVibration() {
    QnPlatformLogic.vibration = OhosCapiAppVibration
}

private object OhosCapiAppVibration : IAppVibration {

    override fun triggerVibration() {
        memScoped {
            val attribute = alloc<Vibrator_Attribute>().apply {
                usage = VIBRATOR_USAGE_UNKNOWN
            }
            OH_Vibrator_PlayVibration(DEFAULT_VIBRATION_DURATION_MS, attribute.readValue())
        }
    }

    private const val DEFAULT_VIBRATION_DURATION_MS = 70
}
