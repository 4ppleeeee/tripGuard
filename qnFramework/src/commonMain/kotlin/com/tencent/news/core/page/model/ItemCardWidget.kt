package com.tencent.news.core.page.model


import com.tencent.news.core.list.model.QnKmmFeedsItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.ITEM_CARD)
class ItemCardWidget(
    @Serializable(ItemWidgetDataWrapperSerializer::class)
    var data: ItemCardWidgetData? = null
) : StructWidget(), IFeedsItemWidget {

    override fun getItemWidgets() = listOfNotNull(data?.item)

    override fun getWidgetType() = StructWidgetType.ITEM_CARD

}

@Serializable
class ItemCardWidgetData(
    var item: QnKmmFeedsItem? = null
) : StructWidgetData()

class ItemWidgetDataWrapperSerializer : DataWrapperSerializer<ItemCardWidgetData>(
    StructWidgetType.ITEM_CARD, ItemCardWidgetData.serializer()
)