package com.tencent.news.core.tads.model

interface IKmmAdVideoInfo {
    val defn: String?       // 视频清晰度
    val hevclv: String?     // 编码视频清晰度
    val height: Int         // 视频高度
    val width: Int          // 视频宽度
    val originWidth: Int    // 视频素材宽度
    val originHeight: Int   // 视频素材高度

}