package com.tencent.news.core.page.model

annotation class StructPageBusinessType {
    companion object {
        const val EMPTY = ""

        // 专题
        const val IP = "ip"
        const val TOPIC = "weibo"
        const val EVENT = "hot_event"
        const val QA = "wenda"
        const val COMMENT = "comment"
        const val VIDEO_TOPIC = "topic"

        // 脉络
        const val EVENT_LINE = "event_line"

        // 付费
        const val COLUMN = "column_detail"

        // 热点
        const val HOT_MODULE_LIST = "hot_module_list"
    }
}
