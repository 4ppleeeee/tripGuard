package com.tencent.news.core.tads.model

interface IAdPlayableMiniGameInfo {
    val playId: String          // 试玩id
    val playInfoJson: String    // 透传给sdk使用，新闻侧不需要解析理解
    val coverUrl: String        // 封面图，解析自playInfoJson的end_page_cover，后续ams添加新字段，下掉该逻辑
}