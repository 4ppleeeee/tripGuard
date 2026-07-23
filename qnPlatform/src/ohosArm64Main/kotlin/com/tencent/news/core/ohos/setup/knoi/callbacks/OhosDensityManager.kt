package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.service.AppService
import kotlin.math.abs

internal object OhosDensityManager {

    // 字号缩放梯度
    val textScaleGradientNormal = arrayOf(0.88, 0.98, 1.087, 1.195, 1.303, 1.303)

    // scale 从 ArkTS number 透传到 Kotlin Double 时可能有极小精度差，匹配兜底保留容差。
    private const val SCALE_MATCH_TOLERANCE = 0.0001

    /**
     * 字号缩放变更回调，修正字号缩放梯度
     */
    fun onTextScaleChanged(scale: Double, level: Int? = null) {
        val targetLevel = level?.takeIf { it in textScaleGradientNormal.indices }
            ?: findMatchedTextScaleLevel(textScaleGradientNormal, scale).takeIf { it >= 0 }
            ?: return
        AppService.status.setTextScaleLevel(targetLevel)
    }

    /**
     * 根据字号缩放梯度获取缩放比例
     */
    fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double {
        return textScaleGradientNormal[gradient.level]
    }

    private fun findMatchedTextScaleLevel(gradient: Array<Double>, scale: Double): Int {
        return gradient.indexOfFirst { abs(it - scale) <= SCALE_MATCH_TOLERANCE }
    }

}
