package com.tencent.news.core.detail

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

/**
 * 措辞纠错数据结构
 */
@Serializable
data class SsmlInfo(
    val position_sentence: String = "",       // 目标语句。举例：“现状是各地的经济水平是参差不齐的。”
    val key: String = "",                    // 语句中的关键词，举例："参差不齐"
    val result: String = "",                 // 关键词换成什么，举例："<phoneme alphabet=\"py\" ph=\"cen1 ci1 bu4 qi2\">参差不齐</phoneme>"
    val type: Int = -1
) : IKmmKeep