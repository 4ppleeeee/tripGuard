package com.tencent.news.core.tads.model

/**
 * 兜底大卡浮层信息
 */
interface IAdFloatingZoneInfo {
    val imageUrl: String
    val descText: String

    fun isValid(): Boolean
}
