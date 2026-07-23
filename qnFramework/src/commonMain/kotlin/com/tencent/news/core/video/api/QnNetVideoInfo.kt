package com.tencent.news.core.video.api

/**
 * 视频 vinfo CGI 返回的关键信息（对齐 Android: TVKNetVideoInfo）
 *
 * KMM 与端上播放器（TVK/iOS VideoNetInfo）的桥接数据模型，
 * 只提取鉴权和 UI 关心的字段；后续如有扩展，保持向后兼容（字段只增不删）。
 *
 * 字段含义（对齐 Android: VideoAuthManager.onNetVideoInfo）：
 * - [mediaVideoState] payState，对齐 TVKNetVideoInfo.mediaVideoState；
 *   业务约定 8 为 VIDEO_FREE_FOR_ALL（免费全集）
 * - [st]              视频状态，对齐 TVKNetVideoInfo.st；
 *   业务约定 2 为 VIDEO_STATE_CAN_PLAY（有权限可播）
 * - [previewDurationSec] 试看时长（秒），对齐 TVKNetVideoInfo.previewDurationSec
 * - [prePlayCountPerDay] 每日可试看次数
 * - [restPrePlayCount]   剩余试看次数
 * - [isPreview]          是否当前请求为试看模式
 */
data class QnNetVideoInfo(
    val mediaVideoState: Int = 0,
    val st: Int = 0,
    val previewDurationSec: Long = 0L,
    val prePlayCountPerDay: Int = 0,
    val restPrePlayCount: Int = 0,
    val isPreview: Boolean = false,
) {
    companion object {
        /** payState：免费全集（对齐 Android VIDEO_FREE_FOR_ALL） */
        const val VIDEO_FREE_FOR_ALL: Int = 8

        /** st：可播放（对齐 Android VIDEO_STATE_CAN_PLAY） */
        const val VIDEO_STATE_CAN_PLAY: Int = 2
    }

    /**
     * 是否可以直接播放
     * 对齐 Android: VideoAuthManager.onNetVideoInfo →
     *     flags.canPlay = payState == VIDEO_FREE_FOR_ALL && st == VIDEO_STATE_CAN_PLAY
     */
    fun canPlay(): Boolean = mediaVideoState == VIDEO_FREE_FOR_ALL && st == VIDEO_STATE_CAN_PLAY

    /**
     * 是否支持试看（previewDurationSec > 0 即认为支持）
     * 对齐 Android: VideoAuthManager.onNetVideoInfo →
     *     flags.hasPreview = !forceNoPreview && netInfo.previewDurationSec > 0
     */
    fun hasPreview(): Boolean = previewDurationSec > 0L
}
