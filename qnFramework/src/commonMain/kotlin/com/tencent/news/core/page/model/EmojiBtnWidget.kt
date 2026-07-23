@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.isNotNullOrBlank
import com.tencent.news.core.list.model.QnKmmHotEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.EMOJI_BTN)
open class EmojiBtnWidget : StructBtnWidget<EmojiBtnWidgetData>() {

    @Serializable(EmojiBtnWidgetDataWrapperSerializer::class)
    override var data: EmojiBtnWidgetData? = null

    override fun getWidgetType() = StructWidgetType.EMOJI_BTN

}

@Serializable
class EmojiBtnWidgetData : StructBtnWidgetData() {
    var hot_event: QnKmmHotEvent? = null
    var emoji: EventEmoji? = null
}

class EmojiBtnWidgetDataWrapperSerializer : DataWrapperSerializer<EmojiBtnWidgetData>(
    StructWidgetType.EMOJI_BTN, EmojiBtnWidgetData.serializer()
)

@Serializable
open class EventEmoji : IKmmKeep {

    var id: String? = null
    var idStr: String? // 适配给ios
        get() = id
        set(value) {
            id = value
        }

    var icon: String? = null
    var name: String? = null
    var lottie: String? = null
    var interaction_num: Long = 0

    fun isValid(): Boolean =
        id.isNotNullOrBlank() && icon.isNotNullOrBlank() && lottie.isNotNullOrBlank()

}