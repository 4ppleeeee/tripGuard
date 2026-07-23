package com.tencent.news.core.video.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.getPlatformDate
import kotlinx.serialization.Serializable

/**
 * 将时间字符串转为秒数，支持 "mm:ss" 和 "HH:mm:ss" 格式
 */
private fun String.toSecondOffsetOrZero(): Long {
    if (isBlank()) return 0L

    return try {
        val parts = trim().split(":")
        when (parts.size) {
            2 -> {
                // "mm:ss" 格式
                val minutes = parts[0].toLongOrNull() ?: 0L
                val seconds = parts[1].toLongOrNull() ?: 0L
                minutes * 60 + seconds
            }
            3 -> {
                // "HH:mm:ss" 格式
                val hours = parts[0].toLongOrNull() ?: 0L
                val minutes = parts[1].toLongOrNull() ?: 0L
                val seconds = parts[2].toLongOrNull() ?: 0L
                hours * 3600 + minutes * 60 + seconds
            }
            else -> 0L
        }
    } catch (e: Exception) {
        0L
    }
}

/**
 * AI 生成的视频章节信息
 * @property start 章节开始时间，如 "01:30" 或 "00:05:30"
 * @property desc 章节标题
 * @property content 章节摘要
 */
@Serializable
class AiChapter(
    var start: String? = null,
    var desc: String? = null,
    var content: String? = null
) : IKmmKeep, IKmmParcelable {
    /** 秒级时间戳，每次访问时计算 */
    @kotlinx.serialization.Transient
    val startSecond: Long
        get() = start?.toSecondOffsetOrZero() ?: 0L

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(start)
        dest.writeString(desc)
        dest.writeString(content)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        start = from.readString()
        desc = from.readString()
        content = from.readString()
    }
}
