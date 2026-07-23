package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable


@Serializable
class AdAppointData : IKmmKeep {

    private val reserve_id: String = ""

    private val publish_time: String = ""

    val reserveId: String
        get() = reserve_id

    val publishTime: String
        get() = publish_time
}