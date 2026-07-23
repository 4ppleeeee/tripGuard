@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(QnCoreStructWidgetType.IP_SHARE_BTN)
open class IpShareBtnWidget : StructBtnWidget<ShareBtnWidgetData>() {

    @Serializable(IpShareBtnWidgetDataWrapperSerializer::class)
    override var data: ShareBtnWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.IP_SHARE_BTN

}

class IpShareBtnWidgetDataWrapperSerializer : DataWrapperSerializer<ShareBtnWidgetData>(
    QnCoreStructWidgetType.IP_SHARE_BTN, ShareBtnWidgetData.serializer()
)
