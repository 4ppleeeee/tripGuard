package com.tencent.news.core.page.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(QnCoreStructWidgetType.PUBLISH_BTN)
open class PublishBtnWidget : StructWidget() {
    override fun getWidgetType() = QnCoreStructWidgetType.PUBLISH_BTN
}
