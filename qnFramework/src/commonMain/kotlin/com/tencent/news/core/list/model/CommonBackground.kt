package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

enum class ExpandStatus(val value: Int) {
    COLLAPSED(0), // 收缩状态
    EXPANDED(1)   // 展开状态
}

@Serializable
data class CommonBackground(
    var imgUrl: String? = null,         // 日间图片
    var imgUrlNight: String? = null,    // 夜间图片
    var width: Int? = null,             // 宽
    var height: Int? = null,            // 高
    var expand_status: Int? = null,
    var highlight: Int? = null,         // 0 默认高亮，1 高亮
) : IKmmKeep