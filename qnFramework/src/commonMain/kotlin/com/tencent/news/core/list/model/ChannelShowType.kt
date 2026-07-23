package com.tencent.news.core.list.model

import com.tencent.news.core.tads.constants.INVALID_NUM


typealias KmmChannelShowType = ChannelShowType

object ChannelShowType {

    const val INVALID = INVALID_NUM     // 非法值

    const val COMMON = 1                // 常规无限刷频道
    const val VIDEO = 2                 // 视频频道
    const val NEWS_LIMIT = 6            // 新闻-有限刷
    const val ATTENTION = 39            // 【大圣可配】热推/新闻-关注

    const val COMPOSE = 47              // 【大圣可配】compose频道

    const val AUDIO_ENTRY = 48          // 【大圣可配】音频首页
    const val WEB = 49                  // 【大圣可配】web频道
    const val HIPPY = 52                // 【大圣可配】hippy频道

    const val IP_CHANNEL = 56           // 有限刷，IP频道（透传给接入层，会将专题转成田字格样式）
    const val LONG_VIDEO = 60           // 【大圣可配】 放映厅长视频频道
    const val LIKE_RADIO = 61           // 【大圣可配】 爱听频道
    const val COMMON_LIST = 143         // 常规列表

    const val UP_DOWN_LIST = 147        // 支持顶部自动加载的列表

    const val DISCUSS_DETAIL = 150      // tag-讨论区底层页

    const val GLOBAL_LIST_DETAIL_PAGE = 152     // 通用综合底层页（通用的：DefaultChildComponentFragment）

    const val PAGE_CHANNEL_724 = 168        // 724频道
    const val CHANNEL_CONTAINER = 169       // 品字形频道容器
    const val CHUPIN_PROGRAM_VIDEO = 170    // 独家出品栏目视频
    const val CHANNEL_ALL_VIDEO_DETAIL = 172    // 全视频底层
    const val TOP_HOME_CHANNEL = 173            // 原来双导航 现在没用了 留在备用 其他业务可以放心覆盖哟～

    const val IP_VIDEO_DETAIL_CHANNEL = 174     // ip详情页
    const val IP_VIDEO_COMMENT_CHANNEL = 175    // ip评论页

    const val LIVE_CHAT = 176                   // 直播-聊天
    const val LIVE_INTRODUCTION = 177           // 直播-简介
    const val LIVE_ANCHOR_HALL = 178            // 直播-主播厅
    const val LIVE_H5_TAB = 179                 // 直播-H5TAB
    const val LIVE_PLAYBACK_DETAIL_TAB = 180    // 直播-详情TAB
    const val LIVE_RELATED_ARTICLE_TAB = 181    // 直播-相关阅读TAB
    const val LIVE_PROGRAM_CALENDAR = 182       // 直播-直播节目栏
    const val LIVE_COLLECTION_TAB = 183         // 直播-集合TAB

    const val QA_CHANNEL = 190          // 热问频道
    const val STAFF_PICK = 191          // 精选频道
    const val STRUCT_EVENT = 192        // 专题
    const val COLUMN = 193              // 专栏

    const val QA_EVENT_SUB_LIST = 194   // 问答专题子tab
    const val OM_LIVE = 195             // cp页直播tab
    const val VIDEO_TOPIC_LIST: Int = 196 // 视频话题列表

    const val HOT_EVENT_WITH_DIRECTORY = 201 // 专题带右侧目录

    const val HOT_MODULE = 400          // 热点精选列表

    const val HOT_TRACK = 401           // 热点追踪

    const val THING = 402               // 事件

    const val POOL_TAG_LIST = 403       // 长线话题

    // --- 以下为车载相关，车载代码不合主干，主干看不到调用逻辑。勿删 ---
    const val CAR_POST = 404            // 汽车-早晚报
    const val CAR_REAL_TIME = 405       // 汽车-实时播报
    const val CAR_PICK = 406            // 汽车-精选
    const val CAR_RECOMMEND = 407       // 汽车-推荐
    // --- 以上为车载相关，车载代码不合主干，主干看不到调用逻辑。勿删 ---

    const val NEWS_STAGGED_GRID_LAYOUT = 408           // 发现频道-瀑布流

    const val FLEXIBLE = 999            // 灵活列表（由结构化协议控制分页）

}
