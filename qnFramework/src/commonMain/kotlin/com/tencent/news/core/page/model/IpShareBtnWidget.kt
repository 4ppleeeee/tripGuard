@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.IP_SHARE_BTN)
open class IpShareBtnWidget : StructBtnWidget<ShareBtnWidgetData>() {

    @Serializable(IpShareBtnWidgetDataWrapperSerializer::class)
    override var data: ShareBtnWidgetData? = null

    override fun getWidgetType() = StructWidgetType.IP_SHARE_BTN

}

class IpShareBtnWidgetDataWrapperSerializer : DataWrapperSerializer<ShareBtnWidgetData>(
    StructWidgetType.IP_SHARE_BTN, ShareBtnWidgetData.serializer()
)