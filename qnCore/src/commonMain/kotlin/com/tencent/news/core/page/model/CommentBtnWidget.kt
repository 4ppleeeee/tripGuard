package com.tencent.news.core.page.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(QnCoreStructWidgetType.COMMENT_BTN)
open class CommentBtnWidget : StructBtnWidget<CommentBtnWidgetData>() {

    @Serializable(CommentBtnWidgetDataWrapperSerializer::class)
    override var data: CommentBtnWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.COMMENT_BTN

}

@Serializable
class CommentBtnWidgetData : StructBtnWidgetData() {
}

class CommentBtnWidgetDataWrapperSerializer : DataWrapperSerializer<CommentBtnWidgetData>(
    QnCoreStructWidgetType.COMMENT_BTN, CommentBtnWidgetData.serializer()
)
