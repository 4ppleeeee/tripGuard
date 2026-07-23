package com.tencent.news.core.page.model


import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(QnCoreStructWidgetType.ASK_BTN)
class AskBtnWidget : StructBtnWidget<AskBtnWidgetData>(), IKmmKeep {

    @Serializable(AskBtnWidgetDataWrapperSerializer::class)
    override var data: AskBtnWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.ASK_BTN

}

@Serializable
class AskBtnWidgetData : StructBtnWidgetData(), IKmmKeep {

    @SerialName("event_id")
    var eventId: String = ""

    var hint: String = ""       // 默认文案

}

class AskBtnWidgetDataWrapperSerializer : DataWrapperSerializer<AskBtnWidgetData>(
    QnCoreStructWidgetType.ASK_BTN, AskBtnWidgetData.serializer()
)
