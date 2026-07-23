@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tag.model.IKmmTagInfo
import com.tencent.news.core.tag.model.QnTagInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(QnCoreStructWidgetType.COLUMN_HEADER)
class ColumnHeaderWidget : HeaderWidget(), IKmmKeep {

    @Serializable(ColumnHeaderWidgetDataWrapperSerializer::class)
    var data: ColumnHeaderWidgetData? = null

    override fun getWidgetType() = QnCoreStructWidgetType.COLUMN_HEADER

    companion object {

        fun create(tagInfo: IKmmTagInfo?, readCountAll: Int): ColumnHeaderWidget {
            return ColumnHeaderWidget().apply {
                data = ColumnHeaderWidgetData().apply {
                    this.tagInfo = tagInfo
                    this.readCountAll = readCountAll
                }
            }
        }

    }

}

@Serializable
class ColumnHeaderWidgetData : StructWidgetData(), IKmmKeep {
    @SerialName("tag_info_item")
    var tagInfo: QnTagInfo? = null

    @SerialName("read_count_all")
    var readCountAll: Int = 0
}

class ColumnHeaderWidgetDataWrapperSerializer : DataWrapperSerializer<ColumnHeaderWidgetData>(
    QnCoreStructWidgetType.COLUMN_HEADER, ColumnHeaderWidgetData.serializer()
)
