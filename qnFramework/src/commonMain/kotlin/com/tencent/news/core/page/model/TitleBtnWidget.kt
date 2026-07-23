@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


annotation class TitleBtnShowType {
    companion object {
        const val DEFAULT = 0
        const val LANTING_FONT = 2      // 用‘兰亭特黑’字体样式
    }
}

@Serializable
@SerialName(StructWidgetType.TITLE_BTN)
open class TitleBtnWidget : StructWidget() {

    @Serializable(TitleBtnWidgetDataWrapperSerializer::class)
    var data: TitleBtnWidgetData? = null

    var action: TitleBtnWidgetAction? = null

    override fun getWidgetType() = StructWidgetType.TITLE_BTN

    companion object {

        fun create(
            title: String?,
            iconUrl: String? = "",
            flagUrl: String? = "",
            isAvatar: Boolean = false,
            fontSize: Float = 16f,
            fontType: Int = TitleBtnShowType.DEFAULT,
            gradientColors: List<StructColor>? = null,
            normalTextColor: StructColor? = null,
            isNightUseNormalTextColor: Boolean = false
        ): TitleBtnWidget {
            return TitleBtnWidget().apply {
                data = TitleBtnWidgetData().apply {
                    this.title = title
                    this.iconUrl = iconUrl
                    this.flagUrl = flagUrl
                    this.isAvatar = isAvatar
                    this.fontSize = fontSize
                    this.fontType = fontType
                    this.gradientColors = gradientColors
                    this.normalTextColor = normalTextColor
                    this.isNightUseNormalTextColor = isNightUseNormalTextColor
                }
            }
        }

    }

}

@Serializable
class TitleBtnWidgetData : StructWidgetData() {
    var title: String? = ""     // 标题（Header区域折叠后才显示）
    var iconUrl: String? = ""   // 标题左侧小图标
    var flagUrl: String? = ""   // 标题icon右下角小标签
    var fontSize: Float = 16f   // 标题大小
    var isAvatar: Boolean = false   // 是否为头像
    var fontType: Int = TitleBtnShowType.DEFAULT   // 字体类型，见 TitleBtnShowType
    var gradientColors: List<StructColor>? = null  // 标题渐变色，不配置则使用标题栏默认色

    var normalTextColor: StructColor? = null       // 字体颜色

    var isNightUseNormalTextColor: Boolean = false  // 是否在夜间模式下使用normalTextColor
}


class TitleBtnWidgetDataWrapperSerializer : DataWrapperSerializer<TitleBtnWidgetData>(
    StructWidgetType.TITLE_BTN, TitleBtnWidgetData.serializer()
)

@Serializable
class TitleBtnWidgetAction : StructWidgetAction() {

}

