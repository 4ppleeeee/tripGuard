@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.LIST_HEADER)
open class ListHeaderWidget : HeaderWidget() {
    override fun getWidgetType() = StructWidgetType.LIST_HEADER
}
