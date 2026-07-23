package com.tencent.news.core.video.constants

/**
 * 视频文章类型枚举
 */
enum class VideoArticleType {
    Default,
    Preload,        // 预加载
    NoCmsid,        // 无cmisd
    Reset           // 超时重置
}