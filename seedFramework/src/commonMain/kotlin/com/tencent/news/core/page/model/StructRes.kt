@file:Suppress("unused")

package com.tencent.news.core.page.model

import com.tencent.news.core.app.constants.LayoutGravity
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.KColor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// todo 架构说明：本文件用于规范化各类资源的数据结构描述，方便两端组件统一

@Serializable
class StructImage : IKmmKeep {
    var normal_style: StructImageUrl? = null
    var dark_style: StructImageUrl? = null // 老的协议设计，目前没实际用过，暂留
    var size: StructSize? = null
}

@Serializable
data class StructText(
    val text: String = "",
    val size: StructSize = StructSize.BOTTOM_ICON_TEXT,
    val gravity: LayoutGravity = LayoutGravity.BOTTOM,
    val color: StructColor = StructColor.T1,
    val spaceSize: StructSize = StructSize(4),
) : IKmmKeep

@Suppress("SerialNameAtPublicClass")
@Serializable // 以前旧版专题header有下发，现在应该没有后台下发的情况了
data class StructImageUrl(
    @SerialName("day_url")
    var dayUrl: String = "",

    @SerialName("night_url")
    var nightUrl: String = dayUrl,
) : IKmmKeep

@Serializable // 目前没下发过，预留了下发解析能力
data class StructColor(
    val dayColor: String = "",
    val nightColor: String = dayColor,
) : IKmmKeep {

    companion object {
        // alphaPercent取值为 [0.0-1.0]
        fun StructColor.changeAlpha(alphaPercent: Float): StructColor {
            return StructColor(
                dayColor = KColor.blendAlpha(dayColor, alphaPercent),
                nightColor = KColor.blendAlpha(nightColor, alphaPercent)
            )
        }

        // 主要文字 : 标题/正文等
        val T1 = StructColor(dayColor = "#333333", nightColor = "#D9D9D9")
        val T1A = StructColor("#1F1F1F", "#FFFFFF")

        // 次要文字 : 引用类文本
        val T2 = StructColor(dayColor = "#5C5C5C", nightColor = "#a9a9a9")
        val T2A = StructColor("#A6A6A6", "#4c4c4c")

        // 辅助文字 : 来源/注释类文本
        val T3 = StructColor(dayColor = "#999999", nightColor = "#696969")

        // 压图文字 : 主要用于在深色底上或带背景图的文本
        val T4 = StructColor(dayColor = "#ffffff", nightColor = "#E6E6E6")

        // 压图次要文字 : 75%透明度
        val T5 = StructColor(dayColor = "#BFFFFFFF", nightColor = "#BFE6E6E6")

        // 按钮文字颜色
        val BTN_MID_NO_NIGHT = StructColor("#E3ECFF")

        // 区块背景
        val BG_BLOCK = StructColor(dayColor = "#F7F7F7", nightColor = "#262626")
        val BG_BLOCK_NO_NIGHT = StructColor(dayColor = "#F7F7F7")

        // 标准蓝 覆盖各类图标/按钮/文字/气泡/文字链/图形插画等
        val B_NORMAL = StructColor(dayColor = "#3377FF", nightColor = "#3071F2")

        val WHITE = StructColor(dayColor = "#FFFFFF", nightColor = "#FFFFFF")
        val BLACK = StructColor(dayColor = "#000000", nightColor = "#000000")

        val WHITE_30 = WHITE.changeAlpha(0.3f)
        val BLACK_70 = BLACK.changeAlpha(0.7f)

        // 通用页面背景色 : 白色页面背景 覆盖范围最广的页面背景
        val BG_PAGE = StructColor(dayColor = "#ffffff", nightColor = "#1f1f1f")
    }

}

fun StructColor.toColorInt(): StructColorInt {
    return StructColorInt(
        dayColor = KColor.toColorInt(dayColor),
        nightColor = KColor.toColorInt(nightColor)
    )
}

@Serializable // 目前没下发过，预留了下发解析能力
data class StructColorInt(
    val dayColor: Int = 0,
    val nightColor: Int = dayColor,
) : IKmmKeep

fun StructColorInt.toColor(): StructColor {
    return StructColor(
        dayColor = KColor.toColorHex(dayColor),
        nightColor = KColor.toColorHex(nightColor)
    )
}

@Serializable // 目前没下发过，预留了下发解析能力
data class StructLottie(
    // lottie url 自带适配日夜间能力
    val urlAndroid: String = "",
    val urlIOS: String = "", // 注意，lottie产物是区分平台的，两端用的不一样
    val urlOhos: String = "", // 注意，lottie产物是区分平台的，两端用的不一样
    val status: String = "",
    val size: StructSize? = null,
) : IKmmKeep

data class StructDrawable(
    val color: StructColor,
    val corner: StructCorner? = null,
)

data class StructCorner(
    // 单位是dp
    val leftTop: Int = 0,
    val rightTop: Int = 0,
    val rightBottom: Int = 0,
    val leftBottom: Int = 0,
) : IKmmKeep {
    companion object {
        const val ROUND = -1

        val ROUND_CORNER = StructCorner(ROUND, ROUND, ROUND, ROUND)
        val RIGHT_ROUND_CORNER = StructCorner(rightTop = ROUND, rightBottom = ROUND)
        val LEFT_ROUND_CORNER = StructCorner(leftTop = ROUND, leftBottom = ROUND)
        val TOP_ROUND_CORNER = StructCorner(leftTop = ROUND, rightTop = ROUND)
        val BOTTOM_ROUND_CORNER = StructCorner(leftBottom = ROUND, rightBottom = ROUND)
    }
}

fun roundCorner(cornerInDp: Int) = StructCorner(cornerInDp, cornerInDp, cornerInDp, cornerInDp)

data class StructBg(
    val drawable: StructDrawable? = null,
    val color: StructColor? = null,
    val padding: StructRect? = null,
    val margin: StructRect? = null,
) : IKmmKeep

data class StructRect(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
) : IKmmKeep

@Suppress("SerialNameAtPublicClass")
@Serializable // 以前旧版专题header有下发，现在应该没有后台下发的情况了
data class StructSize(
    // 宽高单位是dp
    val width: Int = 0, // 如果是iconFont，这个也代表字号
    val height: Int = width,

    @SerialName("aspect_ratio")
    var aspectRatio: Float = 0f,
) : IKmmKeep {

    companion object {
        const val MATCH_PARENT = -1001 // 避免-1的概念容易有冲突，另起一个特殊号段
        const val WRAP_CONTENT = -1002

        val TOP_ICON = StructSize(24)       // TitleBar 常规icon尺寸
        val TOP_TEXT = StructSize(16)       // TitleBar 常规文字尺寸
        val BOTTOM_ICON = StructSize(24)    // BottomBar 常规icon尺寸，与目前 buttons_style.xml 里配置一致
        val BOTTOM_ICON_TEXT = StructSize(10) // 图标文字的字号
        val TIPS_TEXT = StructSize(12)      // tips

        val LIST_BAR_ICON_FONT = StructSize(20) // list bar iconfont的字号
        val LIST_BAR_TEXT = StructSize(14) // list bar text的字号
        val LIST_BAR_LOTTIE = StructSize(24) // list bar lottie的尺寸
    }

}

@Serializable
data class StructPadding(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
) : IKmmKeep {
    companion object {
        val LIST_BAR_LOTTIE = StructPadding(2, 2, 2, 2) // list bar lottie的padding
    }
}
