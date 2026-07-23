package com.tencent.news.core.video.api

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure

interface IQnVideoDataHandler {
    fun bindData(data: IQnVideoData)
}

interface IQnVideoData : IKmmKeep, IKmmPure {
    val vid: String?
    val url: String?
    val coverUrl: String?

    /**
     * 片头时长（秒），来自 `videoChannel.video.unionExtra.head_time`，无则为 0。
     *
     * 用于驱动端上「跳过片头尾」开关在 TVK 真正生效（START_POSITION_MS）。
     * 必须与 [vid] 在同一帧 bindData 下发，避免端上 setDataSource 时拿不到。
     * 对齐 Android: `VideoOpenParam.skipStartPos`，由 `SkipHeadTailBehavior.beforeOpen` 在 open 前一帧赋值。
     */
    val headTimeSec: Int get() = 0

    /**
     * 片尾时长（秒），来自 `videoChannel.video.unionExtra.tail_time`，无则为 0。
     *
     * 用于驱动端上「跳过片头尾」开关在 TVK 真正生效（SKIP_END_POSITION_MS）。
     * 与 [headTimeSec] 同步下发。
     */
    val tailTimeSec: Int get() = 0
}