package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable


/**
 * 电台主题信息
 * 用于表示音频数据所属的电台
 */
interface ITopicStation : IKmmKeep {
    val topicStationId: String    // 电台ID
}

@Suppress("ConstructorParameterNaming", "VariableNaming")
@Serializable
class TopicStation : BaseKmmModel(), ITopicStation, IKmmKeep {

    private var topic_station_id: String = ""

    override val topicStationId: String
        get() = topic_station_id
}
