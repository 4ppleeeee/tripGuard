package com.tencent.news.core.list.constants

data class ListRefreshActionData(
    val indexKey: String = "", // 用来索引列表组件的key，不同action下定义可能不同（例如：footer展开的section.name）
)