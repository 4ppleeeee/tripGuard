@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.list.vm.IClickVM
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.SCHEME_BTN)
class SchemeBtnWidget : StructBtnWidget<SchemeBtnWidgetData>() {

    @Serializable(SchemeBtnWidgetDataWrapperSerializer::class)
    override var data: SchemeBtnWidgetData? = SchemeBtnWidgetData()

    override fun getWidgetType() = StructWidgetType.SCHEME_BTN


    companion object {
        fun create(
            scheme: String,
            image: StructImageUrl? = null,
            textAlignment: StructTextAlignment = StructTextAlignment.TOP,
            iconFont: IconFont? = null,
            lottie: StructLottie? = null,
            size: StructSize = StructSize.BOTTOM_ICON,
            text: StructText? = null,
            clickVM: IClickVM? = null,
            dtElementId: DtElementId? = null,
            forceUseCustomColor: Boolean = false,
            contentDescription: String? = null,
            hasRedDot: Boolean = false,
            dtElementParams: Map<String, Any>? = null,
            dtEnableExposure: Boolean = false
        ): SchemeBtnWidget {
            return SchemeBtnWidget().apply {
                val data = SchemeBtnWidgetData()
                this.data = data

                data.jumpScheme = scheme

                data.icon = StructImage().apply {
                    normal_style = image
                    this.size = size
                }

                data.iconFont = iconFont

                data.lottie = lottie

                data.btnText = text

                data.clickVM = clickVM

                data.dtEid = dtElementId

                data.dtElementParams = dtElementParams

                data.dtEnableExposure = dtEnableExposure

                data.contentDescription = contentDescription

                data.textAlignment = textAlignment

                data.forceUseCustomTextColor = forceUseCustomColor

                data.hasRedDot = hasRedDot
            }
        }
    }

}

@Serializable
open class SchemeBtnWidgetData : StructBtnWidgetData() {
    var jumpScheme: String = ""
}

class SchemeBtnWidgetDataWrapperSerializer : DataWrapperSerializer<SchemeBtnWidgetData>(
    StructWidgetType.SCHEME_BTN, SchemeBtnWidgetData.serializer()
)

enum class StructTextAlignment {
    TOP,
    BOTTOM
}
