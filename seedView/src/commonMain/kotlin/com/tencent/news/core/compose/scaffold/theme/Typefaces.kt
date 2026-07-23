package com.tencent.news.core.compose.scaffold.theme

import com.tencent.kuikly.compose.resources.Font
import com.tencent.kuikly.compose.resources.FontResource
import com.tencent.kuikly.compose.resources.InternalResourceApi
import com.tencent.kuikly.compose.ui.text.font.toFontFamily
import com.tencent.news.core.isAndroidPlatform

enum class Fonts(val family: String) {
    ICONFONT("iconfont"),
    DEFAULT("default"),
    LantingheiSC("FZLanTingHeiS-H-GB"),
    TTTGBMedium("TTTGBMedium"),
    DINAlternate("DINAlternate"),
    PINGFANG("PingFang"),
    AVENIR("avenir"),
    SanSerifLight("SansSerifLight"),
    DREAM_HAN_SERIF("DreamHanSerifCN-W7"),
    DIN_MITTELSCHRIFT("DINMittelschriftLTW1G");

    @OptIn(InternalResourceApi::class)
    val res: FontResource get() = FontResource(family)
}

val iconFontFamily = Font(Fonts.ICONFONT.res).toFontFamily()
val defaultFamily = Font(Fonts.DEFAULT.res).toFontFamily()
val lantingheiFamily = Font(Fonts.LantingheiSC.res).toFontFamily()
val tTTGBMediumFamily = Font(Fonts.TTTGBMedium.res).toFontFamily()
val dinAlternateFamily = Font(Fonts.DINAlternate.res).toFontFamily()
val avenirFamily = Font(Fonts.AVENIR.res).toFontFamily()
val pingFangFamily = Font(Fonts.PINGFANG.res).toFontFamily()
val dreamHanSerifFamily = Font(Fonts.DREAM_HAN_SERIF.res).toFontFamily()
val sanSerifLightFamily = Font(Fonts.SanSerifLight.res).toFontFamily()
val dinMittelschriftFamily = Font(Fonts.DIN_MITTELSCHRIFT.res).toFontFamily()

/**
 * 对文字粗细，实际上安卓只支持normal/medium/bold，因此设置fontWeight=W100/W200/W300时是看不出来变细的
 * 但鸿蒙和ios是支持的，因此这里兼容一下，安卓上用sanSerifLightFamily来显示细体
 */
fun compatFontFamilyForThin() = if (isAndroidPlatform()) {
    sanSerifLightFamily
} else {
    null
}