package com.tencent.news.core.video.api

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure

interface IQnVideoUiController {
    fun updateConfig(config: IQnVideoUiConfig)
    fun setScaleType(scaleType: Int)
}


interface IQnVideoUiConfig : IKmmKeep, IKmmPure {
    val isShowControllerUi: Boolean // 是否展示控制浮层
    val isShowMuteIcon: Boolean     // 是否展示静音按钮
}


object QnVideoScaleType {
    const val ORIGINAL_FULLSCREEN = 1
    const val ORIGINAL_RATIO = 2
}

