package com.tencent.news.core.page.model

import com.tencent.news.core.list.model.QnKmmHotEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(QnCoreStructWidgetType.INPUT_BTN)
open class InputBtnWidget : StructBtnWidget<InputBtnWidgetData>() {

    @Serializable(InputBtnWidgetDataWrapperSerializer::class)
    override var data: InputBtnWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.INPUT_BTN
}

@Serializable
class InputBtnWidgetData : StructBtnWidgetData() {
    var hot_event: QnKmmHotEvent? = null
    var comment_id: String = ""
    var discussion_count: Int = 0
    var text: String = ""
}

class InputBtnWidgetDataWrapperSerializer : DataWrapperSerializer<InputBtnWidgetData>(
    QnCoreStructWidgetType.INPUT_BTN, InputBtnWidgetData.serializer()
)
