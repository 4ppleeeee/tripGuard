package com.tencent.news.core.extension


// 防止高频触发的工具
class FastActionShield(
    private val threshold: Long,     // 频控阈值，单位ms
) {

    private var lastActionTime = 0L

    fun isFastAction(): Boolean {
        val curTime = getCurTimestampMillis()
        val timeOffset = curTime - lastActionTime

        if (timeOffset in 1..threshold) {
            return true
        }

        lastActionTime = curTime
        return false
    }

    fun reset() {
        lastActionTime = getCurTimestampMillis()
    }

}