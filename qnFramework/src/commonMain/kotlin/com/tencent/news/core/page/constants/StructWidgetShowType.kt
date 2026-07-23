package com.tencent.news.core.page.constants

object StructWidgetShowType {

    const val DEFAULT = 0
    const val AUDIO_POD_FOLLOW_PAGE = 1000
    object ChannelBar {
        const val AIGC_DISCOVERY = 100          // AI发现页

        const val AUDIO_POD_CAST_ALBUM = 110    // 音频-播客频道：播客合集页面

        const val AUDIO_POD_FOLLOW = 130        // 播客关注/播客精选 页面

        const val PRESENT_CARD_LIST = 120       // 付费-礼品卡列表

        const val CP_RECOMMEND_LIST = 125       // CP推荐列表页
        
        const val AUDIO_RADIO = 140             // 电台音频页面

        const val TV_CATEGORY = 150             // 长视频分类筛选页（电影/电视剧/综艺等）

        const val QA_EVENT = 160                // 问答专题（事件页/wenda）
    }
}