package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.service.AppService

internal object OhosDensityManager {

    // 字号缩放梯度
    val textScaleGradientNormal = arrayOf(0.9, 1.0, 1.11, 1.22, 1.33, 1.67)

    /**
     * 字号缩放变更回调，修正字号缩放梯度
     */
    fun onTextScaleChanged(scale: Double) {
        val level = textScaleGradientNormal.indexOf(scale)
        AppService.status.setTextScaleLevel(level)
    }

    /**
     * 根据字号缩放梯度获取缩放比例
     */
    fun getScaleRatioByGradient(gradient: DensityScaleGradient): Double {
        return textScaleGradientNormal[gradient.level]
    }

}