package com.tencent.news.core.page.model

import com.tencent.news.core.page.biz.column.ColumnGift
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// 【付费专栏】购买赠送咨询 按钮
// https://universal-1258344701.shiply-cdn.qq.com/config_template/183/1715226544844/rc-upload-1715226501410-4.png
@Serializable
@SerialName(StructWidgetType.COLUMN_GIFT_BTN)
class ColumnGiftBtnWidget : StructBtnWidget<ColumnGiftBtnWidgetData>() {

    @Serializable(ColumnGiftBtnWidgetDataWrapperSerializer::class)
    override var data: ColumnGiftBtnWidgetData? = null

    override fun getWidgetType() = StructWidgetType.COLUMN_GIFT_BTN

}


@Serializable
class ColumnGiftBtnWidgetData : StructBtnWidgetData() {
    var giftData: ColumnGift? = null
}

class ColumnGiftBtnWidgetDataWrapperSerializer : DataWrapperSerializer<ColumnGiftBtnWidgetData>(
    StructWidgetType.COLUMN_GIFT_BTN, ColumnGiftBtnWidgetData.serializer()
)