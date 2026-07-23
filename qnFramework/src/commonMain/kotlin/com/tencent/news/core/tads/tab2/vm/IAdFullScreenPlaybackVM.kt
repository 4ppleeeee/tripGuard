package com.tencent.news.core.tads.tab2.vm

import kotlinx.coroutines.flow.SharedFlow

/**
 * 全屏广告播放驱动通道
 */
interface IAdFullScreenPlaybackVM {
    val playbackSignalFlow: SharedFlow<AdFullScreenPlaybackSignal>
    fun requestPlay()
    fun requestPause()
}
