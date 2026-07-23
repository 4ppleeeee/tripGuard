package com.tencent.news.core.page.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.COMMENT_BTN)
open class CommentBtnWidget : StructBtnWidget<CommentBtnWidgetData>() {

    @Serializable(CommentBtnWidgetDataWrapperSerializer::class)
    override var data: CommentBtnWidgetData? = null

    override fun getWidgetType() = StructWidgetType.COMMENT_BTN

}

@Serializable
class CommentBtnWidgetData : StructBtnWidgetData() {
}

class CommentBtnWidgetDataWrapperSerializer : DataWrapperSerializer<CommentBtnWidgetData>(
    StructWidgetType.COMMENT_BTN, CommentBtnWidgetData.serializer()
)