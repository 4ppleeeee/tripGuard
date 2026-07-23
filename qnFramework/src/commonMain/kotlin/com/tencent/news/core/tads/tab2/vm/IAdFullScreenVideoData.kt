package com.tencent.news.core.tads.tab2.vm

/**
 * 全屏广告视频数据：视频播放所需的只读字段，与页面 VM 其它职责解耦。
 */
interface IAdFullScreenVideoData {
    val videoId: String
    val videoUrl: String
    val coverUrl: String
    /** 是否竖版视频（视觉规格：9:16） */
    val isPortraitVideo: Boolean
    /** 视频宽高比（width / height），竖版 < 1 */
    val videoAspectRatio: Float
}
