package com.tencent.news.core.compose.scaffold

class PagerScrollState(
    val scrollToPosition: Int = -1,         // 滑动到指定索引位置（-1表示不生效，优先级第四）
    val animated: Boolean = false,           // 是否有滑动动画
)