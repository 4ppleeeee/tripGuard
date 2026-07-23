package com.tencent.news.core.video.api

interface IQnVideoProgressManager {
    fun registerListener(listener: IQnVideoProgressListener?)
    fun unRegisterListener(listener: IQnVideoProgressListener?)
}

/**
 * 视频播放进度回调（宿主 → KMM）。
 *
 * 三端宿主（Android / iOS / Harmony）已统一以「毫秒」上报 [position] 与 [duration]。
 *
 * @param position 当前播放位置（毫秒）
 * @param duration 视频总时长（毫秒），未知时为 0
 * @param bufferPercent 缓冲百分比，取值 0~100
 */
fun interface IQnVideoProgressListener {
    fun onProgress(position: Long, duration: Long, bufferPercent: Int)
}