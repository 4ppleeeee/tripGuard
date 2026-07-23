package com.tencent.news.core.compose.utils

import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha
import com.tencent.news.core.extension.KColor

private val InvalidColor = Color.Black.changeAlpha(0f)

fun String.parseValidColor(): Color? {
    return if (KColor.isValidColor(this)) {
        parseColor().takeIf { it != InvalidColor }
    } else {
        null
    }
}

/**
 * 将颜色字符串解析为 Color 对象
 * 支持格式：#RGB, #ARGB, #RRGGBB, #AARRGGBB
 */
fun String.parseColor(): Color {
    if (!this.startsWith("#")) return InvalidColor

    val hex = this.substring(1)
    val expandedHex = when (hex.length) {
        3 -> "ff${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}"
        4 -> "${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}"
        6 -> "ff$hex"
        8 -> hex
        else -> return InvalidColor
    }

    return try {
        Color(expandedHex.toLong(16))
    } catch (e: NumberFormatException) {
        InvalidColor
    }
}
