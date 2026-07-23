package com.tencent.news.core.page.model

import com.tencent.news.core.app.constants.IIconFont
import com.tencent.news.core.dt.constants.IDtElementId
import com.tencent.news.core.list.vm.IClickVM
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(QnCoreStructWidgetType.AUDIO_BTN)
class AudioBtnWidget : StructBtnWidget<AudioBtnWidgetData>() {

    @Serializable(AudioBtnWidgetDataWrapperSerializer::class)
    override var data: AudioBtnWidgetData? = AudioBtnWidgetData()

    override fun getWidgetType() = QnCoreStructWidgetType.AUDIO_BTN


    companion object {
        fun create(
            image: StructImageUrl? = null,
            iconFont: IIconFont? = null,
            lottie: StructLottie? = null,
            size: StructSize = StructSize.BOTTOM_ICON,
            text: StructText? = null,
            clickVM: IClickVM? = null,
            dtElementId: IDtElementId? = null,
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
    QnCoreStructWidgetType.AUDIO_BTN, AudioBtnWidgetData.serializer()
)
