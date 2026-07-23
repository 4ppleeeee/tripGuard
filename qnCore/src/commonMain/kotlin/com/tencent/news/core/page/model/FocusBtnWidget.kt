@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.list.model.QnKmmHotEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

annotation class FocusBtnShowType {

    companion object {
        const val QA_EVENT = 1
    }
}

@Serializable
@SerialName(QnCoreStructWidgetType.FOCUS_BTN)
open class FocusBtnWidget : StructWidget() {

    @Serializable(FocusBtnWidgetDataWrapperSerializer::class)
    var data: FocusBtnWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.FOCUS_BTN
}

@Serializable
class FocusBtnWidgetData : StructWidgetData() {
    var hot_event: QnKmmHotEvent? = null
    val collect_count: Long = 0
}

class FocusBtnWidgetDataWrapperSerializer : DataWrapperSerializer<FocusBtnWidgetData>(
    QnCoreStructWidgetType.FOCUS_BTN, FocusBtnWidgetData.serializer()
)
