package com.tencent.news.core.page.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.IP_BOTTOM_BAR)
open class IpBottomBarWidget : BottomBarWidget() {
    override fun getWidgetType() = StructWidgetType.IP_BOTTOM_BAR
}