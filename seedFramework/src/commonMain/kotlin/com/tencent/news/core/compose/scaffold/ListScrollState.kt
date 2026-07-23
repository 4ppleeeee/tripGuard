package com.tencent.news.core.compose.scaffold

import com.tencent.news.core.list.model.IKmmFeedsItem

// 不要使用data class, 因其自动重写了equals和hashcode方法
class ListScrollState(
    val scrollToTop: Boolean = false,       // 滑动到列表顶部（优先级最高）
    val scrollToBottom: Boolean = false,    // 滑动到列表底部（优先级第二）
    val scrollChannelBarToTop: Boolean = false,       // 滑动到列表顶部（优先级第三）
    val scrollToPosition: Int = -1,         // 滑动到指定索引位置（-1表示不生效，优先级第四）
    val currentItem: IKmmFeedsItem? = null, // 滑动到指定item（没有上面4个条件时才生效）
    val animate: Boolean = false,           // 是否有滑动动画
    val scrollRootHeader: Boolean = false,  // 滚动到列表某个位置时，是否要折叠header（配合currentItem使用）
    val fixBottomScollState: Boolean = false,   // kuikly列表bug：listState有时底部滑动状态判断不对，需反向滑动一下才能校准
    val scrollByOffsetY: Float = 0f,
)