package com.tencent.news.core.page.model

import com.tencent.news.core.tag.model.IKmmTagInfo
import com.tencent.news.core.tag.model.QnTagInfo
import com.tencent.news.core.user.model.IUserInfo
import com.tencent.news.core.user.model.QnUserInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// 【付费专栏】购买按钮
@Serializable
@SerialName(QnCoreStructWidgetType.COLUMN_PAY_BTN)
open class ColumnPayBtnWidget : StructBtnWidget<ColumnPayBtnWidgetData>() {

    @Serializable(ColumnPayBtnWidgetDataWrapperSerializer::class)
    override var data: ColumnPayBtnWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.COLUMN_PAY_BTN

    companion object {
        fun create(tagInfo: IKmmTagInfo?, card: IUserInfo?): ColumnPayBtnWidget {
            return ColumnPayBtnWidget().apply {
                data = ColumnPayBtnWidgetData().apply {
                    this.tagInfo = tagInfo
                    this.cardInfo = card
                }

                ui = StructActionBtnWidgetUI().apply {
                    btn_style = BtnStyle().apply {
                        style_id = "column_detail_pay_with_bottom_name"
                    }
                }
            }
        }
    }

}

@Serializable
class ColumnPayBtnWidgetData : StructBtnWidgetData() {
    var tagInfo: QnTagInfo? = null

    var cardInfo: QnUserInfo? = null

    var isColumnPay: Boolean = false
}

class ColumnPayBtnWidgetDataWrapperSerializer : DataWrapperSerializer<ColumnPayBtnWidgetData>(
    QnCoreStructWidgetType.COLUMN_PAY_BTN, ColumnPayBtnWidgetData.serializer()
)
