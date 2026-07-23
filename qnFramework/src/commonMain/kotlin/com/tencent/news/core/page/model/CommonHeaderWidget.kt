@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.COMMON_HEADER)
open class CommonHeaderWidget : HeaderWidget() {

    @Serializable(CommonHeaderWidgetDataWrapperSerializer::class)
    var data: HeaderWidgetData? = null

    override fun getWidgetType() = StructWidgetType.COMMON_HEADER

}

class CommonHeaderWidgetDataWrapperSerializer : DataWrapperSerializer<HeaderWidgetData>(
    StructWidgetType.COMMON_HEADER, HeaderWidgetData.serializer()
)