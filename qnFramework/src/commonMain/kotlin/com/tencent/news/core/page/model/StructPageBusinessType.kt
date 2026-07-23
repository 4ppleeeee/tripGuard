package com.tencent.news.core.page.model

annotation class StructPageBusinessType {
    companion object {
        const val EMPTY = ""

        // 专题：
        const val IP = "ip"                 // ip专题
        const val TOPIC = "weibo"           // 话题专题
        const val EVENT = "hot_event"       // 事件
        const val QA = "wenda"              // 问答专题
        const val COMMENT = "comment"       // 评论专题
        const val VIDEO_TOPIC = "topic"     // 视频话题专题

        // 脉络：
        const val EVENT_LINE = "event_line" // 事件脉络

        // 付费：
        const val COLUMN = "column_detail"  // 付费专栏

        // 热点：
        const val HOT_MODULE_LIST = "hot_module_list" // 热点精选列表

    }
}