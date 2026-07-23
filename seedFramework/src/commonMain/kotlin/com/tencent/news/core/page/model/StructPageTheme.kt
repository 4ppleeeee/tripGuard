package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.KColor
import com.tencent.news.core.platform.api.getShiplySwitch
import kotlinx.serialization.Serializable


data class SkinColor(
    val day: String,
    val night: String,

    val dayInt: Int = KColor.toColorInt(day),
    val nightInt: Int = KColor.toColorInt(night),
) : IKmmKeep


data class SkinImage(
    val day: String,
    val night: String,
) : IKmmKeep

@Suppress("PropertyName")
@Serializable

class StructPageTheme : IKmmKeep {
    var skin_color: String = ""             // 【必填】主题色

    var focus_color: String = ""            // 关注按钮-背景色
    var focus_text_color: String = ""       // 关注按钮-字色

    var bar_bg_color: String = ""           // 底条-背景色
    var bar_bg_image: String = ""           // 底条-背景图
    var bar_comment_color: String = ""      // 底条-评论输入框背景色
    var bar_icon_show_type: Int = 0         // 底部-icon色值类型: 0-浅色背景黑icon(同日间样式)，1-深色背景白icon(同夜间样式)

    var com_focus_color: String = ""
    var com_focus_text_color: String = ""
    var com_all_title_color: String = ""

    var catalogue_color: String = skin_color    // 目录导航色值（客户端本地用）

    var skin_switch: Int = 0                // 夜间模式开启混色模式： 0 打开 1 关闭

    var close_gradual_change = 0 // 关闭头部渐变，默认开启

    fun isDataValid(): Boolean {
        return KColor.isValidColor(skin_color)
    }

    fun mapToNightTheme(): StructPageTheme {
        val day = this
        return StructPageTheme().apply {
            if (day.forbidNightSkinMix()) {
                skin_color = day.skin_color

                focus_color = day.focus_color
                focus_text_color = day.focus_text_color

                bar_bg_color = day.bar_bg_color
                bar_comment_color = day.bar_comment_color

                com_focus_color = day.com_focus_color
                com_focus_text_color = day.com_focus_text_color
            } else {
                skin_color = day.skin_color.mapToNightColor()

                focus_color = day.focus_color.mapToNightColor()
                focus_text_color = day.focus_text_color.mapToNightColor()

                bar_bg_color = day.bar_bg_color.mapToNightColor()
                bar_comment_color = day.bar_comment_color.mapToNightColor()

                com_focus_color = day.com_focus_color.mapToNightColor()
                com_focus_text_color = day.com_focus_text_color.mapToNightColor()
            }

            bar_bg_image = day.bar_bg_image
            bar_icon_show_type = day.bar_icon_show_type

            // 【特殊】设计逻辑：夜间文字用灰色
            com_all_title_color = DEFAULT_NIGHT_TEXT_COLOR
            catalogue_color = DEFAULT_NIGHT_TEXT_COLOR

            close_gradual_change = day.close_gradual_change
            skin_switch = day.skin_switch
        }
    }

    private fun forbidNightSkinMix(): Boolean {
        return skin_switch == 1 || getShiplySwitch("night_page_skin_same_with_day")
    }

    private fun String.mapToNightColor(): String {
        if (this.isEmpty()) {
            return ""
        }
        return KColor.blendARGB(this, "#000000", 0.25f) // 设计要求：混合25%黑色蒙层
    }

}

data class PageSkinRes constructor(
    val day: StructPageTheme,
    val night: StructPageTheme,
) : IKmmKeep {

    var skinColor: SkinColor? = null            // 【必填】主题色

    var focusColor: SkinColor? = null           // 关注按钮-背景色
    var focusTextColor: SkinColor? = null       // 关注按钮-字色

    var barBgColor: SkinColor? = null           // 底条-背景色
    var barBgImage: SkinImage? = null           // 底条-背景图
    var barCommentColor: SkinColor? = null      // 底条-评论输入框背景色

    var comFocusColor: SkinColor? = null        // 通用-关注按钮-背景色
    var comFocusTextColor: SkinColor? = null    // 通用-关注按钮-字色
    var comAllTitleColor: SkinColor? = null     // 通用-字色

    var catalogueColor: SkinColor? = null       // 导航目录字色

    init {
        buildColors()
    }

    private fun buildColors() {
        skinColor = createValidColor(day.skin_color, night.skin_color)
        focusColor = createValidColor(day.focus_color, night.focus_color)
        focusTextColor = createValidColor(day.focus_text_color, night.focus_text_color)
        barBgColor = createValidColor(day.bar_bg_color, night.bar_bg_color)
        barBgImage = createValidImage(day.bar_bg_image, night.bar_bg_image)
        barCommentColor = createValidColor(day.bar_comment_color, night.bar_comment_color)
        comFocusColor = createValidColor(day.com_focus_color, night.com_focus_color)
        comFocusTextColor = createValidColor(day.com_focus_text_color, night.com_focus_text_color)
        comAllTitleColor = createValidColor(day.com_all_title_color, night.com_all_title_color)
        catalogueColor = createValidColor(day.catalogue_color, night.catalogue_color)
    }

    private fun createValidColor(day: String, night: String): SkinColor? {
        if (!KColor.isValidColor(day)) {
            return null
        }
        if (!KColor.isValidColor(night)) {
            return SkinColor(day, day)
        } else {
            return SkinColor(day, night)
        }
    }

    private fun createValidImage(day: String, night: String): SkinImage? {
        if (day.isEmpty()) {
            return null
        }
        if (night.isEmpty()) {
            return SkinImage(day, day)
        } else {
            return SkinImage(day, night)
        }
    }

}

const val DEFAULT_NIGHT_TEXT_COLOR = "#D9D9D9"

fun PageSkinRes?.hasSkin(): Boolean {
    return this?.skinColor != null
}

fun PageSkinRes?.hasBarSkin(): Boolean {
    this ?: return false
    return barBgColor != null || barBgImage != null
}

// true: 深色背景白icon(同夜间样式)
fun PageSkinRes?.isBarIconDarkMode(): Boolean {
    if (!hasBarSkin()) {
        return false // 没有底条皮肤时，icon黑白模式不生效
    }
    return 1 == this?.day?.bar_icon_show_type
}