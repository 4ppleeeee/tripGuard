package com.tencent.news.core.list.model

object RequestCgi {

    const val EVENT_DETAIL = "gw/page/event_detail"             // 结构化接口：事件页

    const val PRELOAD_EVENT_DETAIL = "gw/page/v2/event_detail"  // 结构化接口：事件页预加载

    const val CHANNEL_FEED = "gw/page/channel_feed"             // 结构化接口：信息流

    const val USER_PAGE = "gw/page/user_page"                   // 结构化接口：个人页

    const val EVENT_TIMELINE = "gw/page/event_content_more"     // 结构化接口：事件脉络

    const val HOT_MODULE_LIST = "news_feed/hot_module_list"     // 非结构化接口：热点精选列表

    const val HOT_TRACK_LIST = "news_feed/hot_track_list"       // 非结构化接口：热点精选列表中热点追踪

    const val POOL_TAG_LIST = "news_feed/pool_tag_news_list"    // 非结构化接口：热点精选列表中长线话题

    const val COLLECTION_DETAIL = "news_feed/free_collection"   // 非结构化接口：合集详情
}