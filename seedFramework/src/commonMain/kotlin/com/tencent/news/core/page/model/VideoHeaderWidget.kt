package com.tencent.news.core.page.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.VIDEO_HEADER)
open class VideoHeaderWidget : HeaderWidget() {
    override fun getWidgetType() = StructWidgetType.VIDEO_HEADER
}