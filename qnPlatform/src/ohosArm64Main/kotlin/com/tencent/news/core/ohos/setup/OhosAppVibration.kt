package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppVibration
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readValue
import platform.devices.OH_Vibrator_PlayVibration
import platform.devices.VIBRATOR_USAGE_UNKNOWN
import platform.devices.Vibrator_Attribute

fun setupOhosAppVibration() {
    QnPlatformLogic.vibration = OhosAppVibration()
}

/**
 * 鸿蒙震动能力实现，基于 CAPI OH_Vibrator_PlayVibration
 * 与 OhosPlatformHapticFeedback 保持一致的 CAPI 桥接方式
 */
internal class OhosAppVibration : IAppVibration {
    @OptIn(ExperimentalForeignApi::class)
    override fun triggerVibration() {
        // 与其他平台默认震动时长保持一致（MEDIUM 强度约 70ms）
        val duration = DEFAULT_DURATION_MS
        memScoped {
            val attribute = alloc<Vibrator_Attribute>().apply {
                usage = VIBRATOR_USAGE_UNKNOWN
            }
            OH_Vibrator_PlayVibration(duration, attribute.readValue())
        }
    }

    companion object {
        private const val DEFAULT_DURATION_MS = 30
    }
}
