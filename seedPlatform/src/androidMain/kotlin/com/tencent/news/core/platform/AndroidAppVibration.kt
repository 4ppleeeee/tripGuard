package com.tencent.news.core.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.platform.api.IAppVibration

/**
 * Android 平台的 IAppVibration 实现。
 * 使用系统 Vibrator 服务触发短震动反馈。
 */
class AndroidAppVibration : IAppVibration {

    @Suppress("DEPRECATION")
    override fun triggerVibration() {
        val context = LocalKmmContext as? Context ?: return
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    DEFAULT_VIBRATION_DURATION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(DEFAULT_VIBRATION_DURATION_MS)
        }
    }

    companion object {
        private const val DEFAULT_VIBRATION_DURATION_MS = 70L
    }
}
