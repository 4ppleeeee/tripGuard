package com.tencent.news.core.page.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.REFRESH_INDICATOR)
class RefreshIndicatorWidget : StructWidget() {

    @Serializable(RefreshIndicatorDataWrapperSerializer::class)
    var data: RefreshIndicatorData? = null

    override fun getWidgetType() = StructWidgetType.REFRESH_INDICATOR

}

@Serializable
class RefreshIndicatorData : StructBtnWidgetData()

class RefreshIndicatorDataWrapperSerializer : DataWrapperSerializer<RefreshIndicatorData>(
    StructWidgetType.REFRESH_INDICATOR, RefreshIndicatorData.serializer()
)