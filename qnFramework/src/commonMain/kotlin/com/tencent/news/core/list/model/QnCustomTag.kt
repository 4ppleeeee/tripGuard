package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

/**
 * 分类标签数据（对齐 Android Keywords）
 *
 * 用于频道列表中 articleType=570 分类入口卡片的 custom_tags 字段
 * 后台下发示例: {"tagid": "1", "tagname": "电影"}
 */
@Serializable
data class QnCustomTag(
    val tagid: String = "",
    val tagname: String = "",
) : IKmmKeep
