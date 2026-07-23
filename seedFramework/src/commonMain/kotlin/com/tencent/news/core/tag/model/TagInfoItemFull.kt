package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

@Serializable
class TagInfoItemFull : IKmmKeep, ITagInfoItem {
    override var basic: QnTagInfo? = null
}