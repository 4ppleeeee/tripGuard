package com.tencent.news.core.compose.share

import com.tencent.kuikly.compose.ui.graphics.painter.Painter

/**
 * 海报预览数据类
 */
data class PostPreviewData(
    val posterImages: List<String?>? = null,
    val posterViews: List<Any>, // 海报View数组
    val posterStyles: List<Any>, // 海报样式数组
    val defaultPlaceholderList: List<Painter>? = null // 默认本地资源数组（可选）
)