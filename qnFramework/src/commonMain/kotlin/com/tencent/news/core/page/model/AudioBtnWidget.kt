package com.tencent.news.core.page.model

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.list.vm.IClickVM
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.AUDIO_BTN)
class AudioBtnWidget : StructBtnWidget<AudioBtnWidgetData>() {

    @Serializable(AudioBtnWidgetDataWrapperSerializer::class)
    override var data: AudioBtnWidgetData? = AudioBtnWidgetData()

    override fun getWidgetType() = StructWidgetType.AUDIO_BTN


    companion object {
        fun create(
            image: StructImageUrl? = null,
            iconFont: IconFont? = null,
            lottie: StructLottie? = null,
            size: StructSize = StructSize.BOTTOM_ICON,
            text: StructText? = null,
            clickVM: IClickVM? = null,
            dtElementId: DtElementId? = null,
        ): AudioBtnWidget {
            return AudioBtnWidget().apply {
                val data = AudioBtnWidgetData()
                this.data = data

                data.icon = StructImage().apply {
                    normal_style = image
                    this.size = size
                }

                data.iconFont = iconFont

                data.lottie = lottie

                data.btnText = text

                data.clickVM = clickVM

                data.dtEid = dtElementId
            }
        }
    }

}

@Serializable
open class AudioBtnWidgetData : StructBtnWidgetData() {

}

class AudioBtnWidgetDataWrapperSerializer : DataWrapperSerializer<AudioBtnWidgetData>(
    StructWidgetType.AUDIO_BTN, AudioBtnWidgetData.serializer()
)