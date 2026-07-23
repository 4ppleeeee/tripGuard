package com.tencent.news.core.list.model

import com.tencent.news.core.tag.model.QnTagInfo
import kotlinx.serialization.Serializable


@Serializable
class RelateTagInfo : BaseKmmModel() {
    var basic: QnTagInfo? = null
}