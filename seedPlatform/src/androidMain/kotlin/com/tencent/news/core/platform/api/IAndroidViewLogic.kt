package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi

interface IAndroidViewLogic {

    fun dpToPx(dp: Float): Int
    fun dpToPxNoScale(dp: Float): Int

    companion object {
        @KmmInternalApi
        var impl: IAndroidViewLogic? = null
    }
}

@OptIn(KmmInternalApi::class)
fun androidViewLogic(): IAndroidViewLogic = IAndroidViewLogic.impl ?: defaultAndroidViewLogic

private val defaultAndroidViewLogic by lazy {
    object : IAndroidViewLogic {
        private val defaultDensity = 3.0f
        override fun dpToPx(dp: Float): Int = (dp * defaultDensity).toInt()
        override fun dpToPxNoScale(dp: Float): Int = (dp * defaultDensity).toInt()
    }
}