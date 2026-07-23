package com.tencent.news.core.compose.scaffold.skin

import androidx.compose.runtime.Immutable
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.theme.ColorScheme
import com.tencent.news.core.compose.scaffold.theme.DarkColorScheme
import com.tencent.news.core.compose.scaffold.theme.LightColorScheme
import com.tencent.news.core.extension.KColor
import com.tencent.news.core.page.model.StructPageTheme

/**
 * 专题页面皮肤颜色方案
 * 从 StructPageTheme 转换而来的 Compose Color 对象
 */
@Immutable
data class PageSkin(
    // 主题色
    val skinColor: Color?,

    // 关注按钮
    val focusColor: Color?,
    val focusTextColor: Color?,

    // 底条
    val barBgColor: Color?,
    val barCommentColor: Color?,
    val barBgImage: String?, // 图片优先级高于背景色

    // 通用
    val comFocusColor: Color?,
    val comFocusTextColor: Color?,
    val comTextColor: Color?,

    // 底部图标颜色
    val iconColor: Color?,
    // 目录导航色
    val catalogueColor: Color?,

    // 头部/底部 icon色值类型
    val barIconShowType: Int,

    // 关闭头部渐变
    val closeGradualChange: Int,
)

/**
 * 将 StructPageTheme 转换为 StructPageSkinColor
 * 将字符串颜色值转换为 Compose Color 对象
 */
fun StructPageTheme.toStructPageSkinColor(isDarkTheme: Boolean): PageSkin {
    return PageSkin(
        skinColor = parseColor(skin_color),
        focusColor = parseColor(focus_color),
        focusTextColor = parseColor(focus_text_color),
        barBgColor = parseColor(bar_bg_color),
        barCommentColor = parseColor(bar_comment_color),
        comFocusColor = parseColor(com_focus_color),
        comFocusTextColor = parseColor(com_focus_text_color),
        comTextColor = parseColor(com_all_title_color),
        catalogueColor = parseColor(catalogue_color),
        barBgImage = bar_bg_image,
        barIconShowType = bar_icon_show_type,
        closeGradualChange = close_gradual_change,
        iconColor = getColorByBarIconShowType(bar_icon_show_type, isDarkTheme)
    )
}

private fun parseColor(colorString: String): Color? {
    if (colorString.isEmpty() || !KColor.isValidColor(colorString)) {
        return null
    }
    return try {
        val colorInt = KColor.toColorInt(colorString)
        Color(colorInt)
    } catch (e: Exception) {
        null
    }
}

/**
 * 根据 barIconShowType 获取对应模式的颜色
 * @param barIconShowType 0-浅色背景黑icon(日间样式)，1-深色背景白icon(夜间样式)
 * @param colorScheme 当前的颜色方案
 * @return 对应模式的颜色
 */
private fun getColorByBarIconShowType(barIconShowType: Int, isDarkTheme: Boolean): Color {
    return if (barIconShowType == 1) {
        LightColorScheme.t4
    } else {
        if (isDarkTheme) {
            DarkColorScheme.t1
        } else {
            LightColorScheme.t1
        }
    }
}