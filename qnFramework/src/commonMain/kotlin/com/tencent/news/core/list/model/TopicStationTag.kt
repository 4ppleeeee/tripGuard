package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure
import kotlinx.serialization.Serializable


/**
 * 电台主题标签信息
 * 用于表示音频数据所属的电台标签
 */
interface ITopicStationTag : IKmmKeep {
    val tagId: String      // 标签ID
    val tagName: String    // 标签名称
}

@Suppress("ConstructorParameterNaming", "VariableNaming")
@Serializable
class TopicStationTag : BaseKmmModel(), ITopicStationTag, IKmmPure {

    private var tag_id: String = ""
    private var tag_name: String = ""

    override val tagId: String get() = tag_id
    override val tagName: String get() = tag_name

}