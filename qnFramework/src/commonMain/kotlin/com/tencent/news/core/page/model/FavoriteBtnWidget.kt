@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import com.tencent.news.core.list.model.QnKmmHotEvent
import com.tencent.news.core.tag.model.QnTagInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.FAVORITE_BTN)
open class FavoriteBtnWidget : StructBtnWidget<FavoriteBtnWidgetData>() {

    @Serializable(FavoriteBtnWidgetDataWrapperSerializer::class)
    override var data: FavoriteBtnWidgetData? = null

    override fun getWidgetType() = StructWidgetType.FAVORITE_BTN

}

@Serializable
class FavoriteBtnWidgetData : StructBtnWidgetData() {
    var hot_event: QnKmmHotEvent? = null
    private var collect_count: Long = 0

    @SerialName("tag_info_item")
    var tagInfo: QnTagInfo? = null

    var id: String = ""

    var idStr: String
        get() = id
        set(value) {
            id = value
        }

    var collectCount: Long
        get() = collect_count
        set(value) {
            collect_count = value
        }

}

class FavoriteBtnWidgetDataWrapperSerializer : DataWrapperSerializer<FavoriteBtnWidgetData>(
    StructWidgetType.FAVORITE_BTN, FavoriteBtnWidgetData.serializer()
)