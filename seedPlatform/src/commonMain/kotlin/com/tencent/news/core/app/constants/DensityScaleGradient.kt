package com.tencent.news.core.app.constants

import com.tencent.news.core.app.constants.DensityScaleGradient.L1
import com.tencent.news.core.app.constants.DensityScaleGradient.entries
import com.tencent.news.core.extension.safeToInt
import com.tencent.news.core.platform.api.appStatus

/**
 * 字号/尺寸的缩放梯度
 */
enum class DensityScaleGradient(val level: Int) {

    L0(0), L1(1), L2(2), L3(3), L4(4), L5(5);

    // 计算当前梯度与指定梯度的缩放倍数
    operator fun div(other: DensityScaleGradient): Float {
        return scaleRatio / other.scaleRatio
    }

    // 当前梯度的缩放系数
    private val scaleRatio: Float
        get() {
            return appStatus().getScaleRatioByGradient(this).toFloat()
        }
}

fun DensityScaleGradient.serializer(): String {
    return this.level.toString()
}

fun String.deserializer(): DensityScaleGradient {
    return entries.find { it.level == this.safeToInt(1) } ?: L1
}