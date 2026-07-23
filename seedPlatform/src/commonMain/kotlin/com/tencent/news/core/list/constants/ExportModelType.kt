package com.tencent.news.core.list.constants

enum class ExportModelType(val key: String) {
    ITEM("item"),
    AD("ad_order"),

    // 这些预留的，目前没用到
    COMMENT("comment"),
    TAG("tag"),
}
