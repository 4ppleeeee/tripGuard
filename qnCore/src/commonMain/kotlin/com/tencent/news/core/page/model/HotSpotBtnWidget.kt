@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.list.model.QnKmmHotEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(QnCoreStructWidgetType.HOTSPOT_BTN)
open class HotSpotBtnWidget : StructBtnWidget<HotSpotBtnWidgetData>() {

    @Serializable(HotSpotBtnWidgetDataWrapperSerializer::class)
    override var data: HotSpotBtnWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.HOTSPOT_BTN
}

@Serializable
class HotSpotBtnWidgetData : StructBtnWidgetData() {
    var hot_event: QnKmmHotEvent? = null
    var hotspot_num: Int = 0
    var comment_id = ""
}

class HotSpotBtnWidgetDataWrapperSerializer : DataWrapperSerializer<HotSpotBtnWidgetData>(
    QnCoreStructWidgetType.HOTSPOT_BTN, HotSpotBtnWidgetData.serializer()
)
