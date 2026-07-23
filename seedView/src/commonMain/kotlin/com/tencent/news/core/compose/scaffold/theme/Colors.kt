package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Brush.Companion.linearGradient
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha

val LocalColorScheme = staticCompositionLocalOf { LightColorScheme }

@Immutable
data class AigcColorScheme(
    val prompt: Color,
    val prompt05: Color,
    val prompt25: Color,
    val bg: Color,
    val bgSendStart: Brush,
    val bgSendEnd: Brush,
    val lineCard: Color,
    val qaText: Color,
    val purple: Color,
    val purpleBg12: Color,
    val purpleButonBg: Color,
    val stroke: Color,
    val entryStart: Color,
    val entryEnd: Color,
    val streamStart: Color,
    val streamEnd: Color,
    val bottomBarBorder: Color,
    val bottomBarVoiceCancelBorder: Color,
    val bottomBarBottomMask: Brush,
    val bottomBarTopMask: Brush,
    val bottomBarShadowSpot: Color,
    val bottomBarShadowAmbient: Color,
    val yuanbaoGreen: Color,
    val yuanbaoGreen05: Color,
    val yuanbaoGreenBg: Color,
    val posterGrayBg: Color,  // 对话列表海报灰色背景
    val posterBlueBg: Color,  // 选中文本渐变海报蓝色背景
    val digitalBgMask: Brush,  // 数字人背景遮罩
)

@Immutable
data class SportColorScheme(
    val textPrimary: Color,         // 主要文字（纯白）
    val reservedBtnBg: Color,       // 已预约按钮背景 rgba(255,255,255,0.1)
    val reservedBtnText: Color,     // 已预约按钮文字 rgba(255,255,255,0.3)
    val textSecondary: Color,       // 描述/信源等辅助文字 rgba(255,255,255,0.5)
    val divider: Color,             // 分割线 rgba(255,255,255,0.12)
    val endedBtnBg: Color,          // 已结束/延期按钮背景 rgba(255,255,255,0.08)
    val highlightBtnBg: Color,      // 集锦/回放按钮背景 rgba(51,119,255,0.3)
    val liveRed: Color,             // 直播中文字/背景
)

@Immutable
data class AdColorScheme(
    val adMidArticleGameScoreColor: Color,   // 中插游戏评分分颜色
    val adMultiImageBgColor: Color,          // 微广多图背景色

    val adVipBannerBgColor: Color,
    val adVipBannerTipsBgColor: Color,
    val adVipBannerPTitleColor: Color,
    val adVipBannerPSubTitleColor: Color,
    val adVipBannerPPriceColor: Color,
    val adVipBannerBTitleColor: Color,

    val gameReserveTitle: Color,

    // 游戏签到背景色
    val adGameCheckInBgColor: Color,

    // 微信小店标识色值
    val storeHaoDianColor: Color,            // 好店标色值
    val storeRGrey30Color: Color,            // R标 grey30（30%透明度）
    val storeRGrey55Color: Color,            // R标 grey55（55%透明度）

    // 游戏日历颜色
    val gameCalendarTitle: Color,            // 游戏日历标题颜色
    val gameCalendarDesc: Color,             // 游戏日历描述颜色
    val gameCalendarTagBg: Color,            // 游戏日历标签背景色
    val gameCalendarCardShadow: Color,       // 游戏日历卡片阴影色
    val gameCalendarAvatarMask: Color,       // 游戏日历头像遮罩色（夜间模式下叠加）

    // 游戏业务颜色
    val game: GameColorScheme,
)


@Immutable
data class GameColorScheme(
    val reserveTitle: Color,                     // 游戏预约标题颜色
    val checkInBgColor: Color,                   // 游戏签到背景色
    val calendarTitle: Color,                    // 游戏日历标题颜色
    val calendarDesc: Color,                     // 游戏日历描述颜色
    val calendarTagBg: Color,                    // 游戏日历标签背景色
    val calendarCardShadow: Color,               // 游戏日历卡片阴影色
    val calendarAvatarMask: Color,               // 游戏日历头像遮罩色（夜间模式下叠加）
    val categoryTabV2DescColor: Color,           // 游戏分类V2描述色
    val categoryTabV2MetaColor: Color,           // 游戏分类V2标签色
    val feedCardBg: Color,                       // 游戏资讯卡片背景色
    val feedCardShadow: Color,                   // 游戏资讯卡片阴影色
    val feedCardTitle: Color,                    // 游戏咨询卡片主标题
    val hangCardTitle: Color,                    // 游戏咨询卡片悬浮卡片标题
    val calendarBorder: Color,                   // 日历模块边框颜色
)

data class UserColorScheme(
    val historyEditBtnDisabled: Color,
    val messageDivider: Color,
)

@Immutable
data class ColorScheme(
    // 标准蓝
    val bNormal: Color,
    val bLight: Color,
    val bMiddle: Color,
    // 灰蓝色-按钮不可用等场景
    val bGray: Color,
    val redNormal: Color,
    val orangeNormal: Color,
    val redBg: Color,
    val assistantText: Color,
    val blueNormal: Color,
    val bgContentPreference: Color,
    val backIconColor: Color,
    val brownNormal: Color,
    // 蓝紫色
    val purpleNormal: Color,
    // 赞助选中背景色（日间：bLight，夜间：#3377FF）
    val sponsorSelectBg: Color,
    // 标准红
    val rNormal: Color,
    // 字体颜色
    val t1: Color,
    val t2: Color,
    val t3: Color,
    val t6: Color,
    val t3DarkAlpha04: Color,
    val t4: Color,
    val t5: Color,
    val tlink: Color,
    // 黄色系
    val yNormal: Color,

    // 背景颜色
    val bgPage: Color,
    val bgBlock: Color,
    val picDefaultColor: Color,
    val panelBgBlock: Color,
    val panelBgPage: Color,
    val bgCard: Color,
    val bgPageGrey: Color,
    val bgPageMidGrey: Color,       // 设计Token: bg_page_midgrey
    val bgBar: Color,               // 设计Token: bg_bar 底bar背景
    val bgTopLight: Color,          // 设计Token: bg_top_light 标签/气泡/toast
    val bgMiddleStandard: Color,    // 设计Token: bg_middle_standard 选中类背景
    val tabContainerBg: Color,      // 设计Token: tab_container_bg Tab 容器背景
    val tabSelectedBg: Color,       // 设计Token: tab_selected_bg Tab 选中背景
    val assistantBg: Color,
    val assistantBgPurple: Color,
    val assistantBgGradient: Brush,
    val bgAssistantSendStart: Brush,
    val bgAssistantSendEnd: Brush,
    val buttonBlock: Color,
    val bgBottomSheetGrey: Color,
    val bgSnackBar: Color,
    val audioPodSelectBgBrush: Brush,
    val audioPodShadowColor: Color,
    val audioPodNormalBgColor: Color,

    // 分割线
    val lineFine: Color,
    val lineStandard: Color,        // 设计Token: line_standard 10%透明度白色分割线
    val lineWide: Color,
    val lineStroke: Color,
    val lineWideVideo: Color,
    val lineInside: Color,
    val lineLight: Color,           // 设计Token: line_light 浅色分割线
    val transparent: Color,

    // 填充色
    val fillPrimary: Color,         // 设计Token: fill_primary
    val fillSecondary: Color,       // 设计Token: fill_secondary
    val fillPurple: Color,          // 设计Token: fill_purple

    // 按钮背景色
    val btnPrimaryDefault: Color,   // 设计Token: btn_primary_default 一级按钮
    val btnPrimaryDisable: Color,   // 设计Token: btn_primary_disable 一级按钮不可点
    val btnSecondaryDefault: Color, // 设计Token: btn_secondary_default 二级按钮
    val btnLightBrand: Color,       // 设计Token: btn_lightbrand_default 浅紫品牌色
    val btnTertiary: Color,         // 设计Token: btn_tertiary_default 彩色背景按钮
    val watchHistoryBtnBgColor: Color, // 观看历史按钮背景色（日间纯白，夜间纯白10%）

    // 反馈色
    val fbError: Color,             // 设计Token: fb_error
    val fbCorrect: Color,           // 设计Token: fb_correct

    // 遮罩蒙层
    val mask20: Color,              // 设计Token: mask_20
    val mask75: Color,              // 设计Token: mask_75

    // 链接色
    val textLinkBlue: Color,        // 设计Token: text_link_blue

    // 25%透明度的阴影
    val shadow50: Color,
    val shadow40: Color,
    val shadow25: Color,
    val shadow6: Color,
    val shadowImage: Brush,

    // 关闭按钮背景色渐变
    val closeBackground: Color,
    // 纯白色透明度为70%的颜色值
    val white70: Color,
    val white80: Color,

    // 热问大事件横滑 左滑查看更多
    val hotAskMore: Color,
    val verticalShadow: Brush,
    val adMiniGameBg: Color,
    val sponsorCardBg: Brush,
    val sponsorProcessBg: Color,
    // 语音波形背景色
    val whiteText: Color,

    // 内容付费颜色配置
    val paymentColorScheme: PaymentColorScheme,

    // 图文合集头部渐变背景色
    val collectionHeaderIconGradientColor1: Color,
    val collectionHeaderIconGradientColor2: Color,
    val greenNormal: Color,

    // 签到相关颜色
    val checkInBgGradientStart: Color,
    val checkInBgGradientEnd: Color,
    val checkInContentBgGradientStart: Color,
    val checkInContentBgGradientEnd: Color,
    val checkInTitleText: Color,
    val checkInAccentText: Color,
    val checkInButtonGradient: Brush,
    val checkInDashedLine: Color,
    val checkInCoinText: Color,
    val checkInCoinTextChecked: Color,
    val mask50: Color,
    val mask60: Color,

    // 微信小店相关颜色
    val oran70: Color,

    val bgGameCard: Color,
    val bgGameShadow: Color,

    // AIGC 业务颜色
    val aigc: AigcColorScheme,

    // 体育赛事颜色
    val sport: SportColorScheme,

    // 广告业务颜色
    val ad: AdColorScheme,

    // 音频已读标题颜色
    val audioReadTitleColor: Color,

    // 用户相关颜色
    val user: UserColorScheme,
)

val LightColorScheme = ColorScheme(
    bNormal = Color(0xFF3377FF),
    bLight = Color(0xFFEBF5FF),
    bMiddle = Color(0xFF505DE5),
    bGray = Color(0xFFC7D5FB),
    redNormal = Color(0xFFFF0055),
    orangeNormal = Color(0xFFFF8A00),
    redBg = Color(0xFFD81306),
    assistantText = Color(0xFF999999),
    blueNormal = Color(0xFF3377FF),
    bgContentPreference = Color(0xFFEFF6FF),
    backIconColor = Color(0xFF333333),
    brownNormal = Color(0xFFC44F1F),
    purpleNormal = Color(0xFF7642F5), // 设计Token: brand_primary
    sponsorSelectBg = Color(0xFFEBF5FF), // 日间使用 bLight
    t1 = Color(0xFF1F1F23),          // 设计Token: text_primary
    t2 = Color(0xFF5C5C5C),          // 设计Token: text_secondary
    t3 = Color(0xFF999999),          // 设计Token: text_tertiary
    t6 = Color(0x4D999999),
    t3DarkAlpha04 = Color(0x66999999),
    t4 = Color(0xFFFFFFFF),
    t5 = Color(0xBFFFFFFF),
    tlink = Color(0xFF776BFF),       // 设计Token: text_link
    yNormal = Color(0xFFFFB700),
    bgPage = Color(0xFFFFFFFF),      // 设计Token: bg_page
    audioPodNormalBgColor = Color(0xFFFFFFFF),
    bgBlock = Color(0xFFF7F7F7),     // 设计Token: bg_block
    panelBgBlock = Color(0xFFF7F7F7),
    panelBgPage = Color(0xFFFFFFFF),
    bgCard = Color(0xFFFFFFFF),      // 设计Token: bg_card
    bgPageGrey = Color(0xFFF5F5F5),  // 设计Token: bg_page_grey
    bgPageMidGrey = Color(0xFFEFEFEF), // 设计Token: bg_page_midgrey
    bgBar = Color(0xFF232327),       // 设计Token: bg_bar
    bgTopLight = Color(0xFFEFEFEF),  // 设计Token: bg_top_light
    bgMiddleStandard = Color(0xFFF6F6F6), // 设计Token: bg_middle_standard
    tabContainerBg = Color(0xFFF2F2F2), // 设计Token: tab_container_bg
    tabSelectedBg = Color(0xFFFFFFFF),  // 设计Token: tab_selected_bg
    assistantBg = Color(0xFF212144),
    assistantBgPurple = Color(0xFF12132B),
    assistantBgGradient = linearGradient(
        colorStops = arrayOf(
            0f to Color(0xFF464488), // 0%
            0.3f to Color(0xFF12132B), // 30%
            0.71f to Color(0xFF12132B), // 71%
            1f to Color(0xFF12132B)  // 100%
        ),
        start = Offset(0.0f, 0f), end = Offset(0f, Float.POSITIVE_INFINITY)
    ),
    bgAssistantSendStart = Brush.verticalGradient(listOf(Color(0xFF12132B), Color(0x0012132B))),
    bgAssistantSendEnd = Brush.verticalGradient(listOf(Color(0x0012132B), Color(0xFF12132B))),
    audioPodSelectBgBrush = Brush.verticalGradient(
        listOf(Color(0xFFEBF5FF), Color(0xFFFFFFFF)),
        endY = 0.3f
    ),
    audioPodShadowColor = Color(0xFF3377FF),
    buttonBlock = Color(0xFFF7F7F7),
    bgBottomSheetGrey = Color(0xFFF5F5F5),
    bgSnackBar = Color(0xFFFFFFFF),
    lineFine = Color(0x1A000000),    // 设计Token: line_standard
    lineStandard = Color(0x1A000000), // 设计Token: line_standard 10%透明度黑色
    lineWide = Color(0XFFF5F5F5),
    lineStroke = Color(0XFFE6E6E6),
    lineWideVideo = Color(0xFFE6E6E6),
    lineInside = Color(0xFFE6E6E6),
    lineLight = Color(0x0D000000),   // 设计Token: line_light
    transparent = Color(0x00FFFFFF),
    fillPrimary = Color(0x1A000000),   // 设计Token: fill_primary
    fillSecondary = Color(0x33FFFFFF), // 设计Token: fill_secondary
    fillPurple = Color(0x33776BFF),    // 设计Token: fill_purple
    btnPrimaryDefault = Color(0xFF7642F5),   // 设计Token: btn_primary_default
    btnPrimaryDisable = Color(0x80FFFFFF),   // 设计Token: btn_primary_disable
    btnSecondaryDefault = Color(0x0D000000), // 设计Token: btn_secondary_default
    btnLightBrand = Color(0xFFECEBFF),       // 设计Token: btn_lightbrand_default
    btnTertiary = Color(0x80FFFFFF),         // 设计Token: btn_tertiary_default
    watchHistoryBtnBgColor = Color(0xFFFFFFFF),
    fbError = Color(0xFFE6574A),     // 设计Token: fb_error
    fbCorrect = Color(0xFF57BE6A),   // 设计Token: fb_correct
    mask20 = Color(0x33000000),      // 设计Token: mask_20
    mask75 = Color(0xBF000000),      // 设计Token: mask_75
    textLinkBlue = Color(0xFF214CA5), // 设计Token: text_link_blue
    shadow50 = Color.Black.changeAlpha(0.5f),
    shadow40 = Color(0x66000000),
    shadow25 = Color(0x40000000),
    shadow6 = Color(0x0F000000),
    shadowImage = Brush.verticalGradient(colors = listOf(Color(0x00FFFFFF), Color(0x00FFFFFF))),
    closeBackground = Color(0x0A000000),
    white70 = Color(0xB2FFFFFF),
    white80 = Color(0xCCFFFFFF),
    hotAskMore = Color(0xFF53586B),
    verticalShadow = Brush.verticalGradient(colors = listOf(Color(0x1A000000), Color(0x00000000))),
    adMiniGameBg = Color(0x99333333),
    sponsorCardBg = Brush.verticalGradient(colors = listOf(Color(0x14FF0055), Color(0x00FFFFFF))),
    sponsorProcessBg = Color(0xFFFFE3E3),
    whiteText = Color(0xFFFFFFFF),
    paymentColorScheme = LightPaymentColorScheme,
    collectionHeaderIconGradientColor1 = Color(0xFFD5D5D5),
    collectionHeaderIconGradientColor2 = Color(0x66D5D5D5),
    greenNormal = Color(0xFF00AA70),

    // 签到相关颜色 - 日间
    checkInBgGradientStart = Color(0xFFFFDABA),
    checkInBgGradientEnd = Color(0xFFFFF4F4),
    checkInContentBgGradientStart = Color(0xFFFFF2F2),
    checkInContentBgGradientEnd = Color(0xFFFFF8F8),
    checkInTitleText = Color(0xFF282828),
    checkInAccentText = Color(0xFFC44F1F),
    checkInButtonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFEA755),
            Color(0xFFFF7658),
            Color(0xFFFF5934)
        ),
        start = Offset(0f, 0.5f),
        end = Offset(1f, 0.5f)
    ),
    checkInDashedLine = Color(0xFFEDB8AB),
    checkInCoinText = Color(0xFFB24D04),
    checkInCoinTextChecked = Color(0xFF333333),
    picDefaultColor = Color(0xFFEAEAEA),
    mask50 = Color(0x80000000),
    mask60 = Color(0x99000000),
    bgGameCard = Color(0xFFFFFFFF),
    oran70 = Color(0xFF3617B3),
    bgGameShadow = Color(0x0F666666),
    audioReadTitleColor = Color(0xFF999999),
    rNormal = Color(0xFFFF4273),      // 设计Token: brand_secondary

    aigc = AigcColorScheme(
        prompt = Color(0xFF505DE5),
        prompt05 = Color(0x0D505DE5),
        prompt25 = Color(0x40505DE5),
        bg = Color(0xFFF2F6FF),
        bgSendStart = Brush.verticalGradient(listOf(Color(0xFFF2F6FF), Color(0x00F2F6FF))),
        bgSendEnd = Brush.verticalGradient(listOf(Color(0x00F2F6FF), Color(0xFFF2F6FF))),
        lineCard = Color(0x9999994D),
        qaText = Color(0xFF505DE5),
        purple = Color(0xFF505DE5),
        purpleBg12 = Color(0x1F505DE5),
        purpleButonBg = Color(0xFF505DE5),
        stroke = Color(0xFFE9EBFF),
        entryStart = Color(0xFFF7F2FF),
        entryEnd = Color(0xFFF2F7FF),
        streamStart = Color(0xFFD6F2E0),
        streamEnd = Color(0xFFF5F5F5),
        bottomBarBorder = Color(0xFF45AD60),
        bottomBarVoiceCancelBorder = Color(0x66FF0055),
        bottomBarBottomMask = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF6FFFE)
            )
        ),
        bottomBarTopMask = Brush.linearGradient(
            colors = listOf(
                Color(0x00FFFFFF),
                Color(0x33A8F0D1)
            )
        ),
        bottomBarShadowSpot = Color(0xFF999999),
        bottomBarShadowAmbient = Color(0xFF999999),
        yuanbaoGreen = Color(0xFF45AD60),
        yuanbaoGreen05 = Color(0x0D45AD60),
        yuanbaoGreenBg = Color(0xFFEDFAF0),
        posterGrayBg = Color(0xFFF6F6F6),  // 对话列表海报灰色背景
        posterBlueBg = Color(0xFF167BFF),   // 选中文本渐变海报蓝色背景
        digitalBgMask = Brush.verticalGradient(
            colors = listOf(
                Color(0x00123338),
                Color(0x66123338),
                Color(0xCC123338),
                Color(0xFF10393F),
                Color(0xFF0D3035)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    ),

    ad = AdColorScheme(
        adMidArticleGameScoreColor = Color(0xFFFF7D3E),
        adMultiImageBgColor = Color(0xFF8A8A8A),
        adVipBannerBgColor = Color(0xFFF5E9CE),
        adVipBannerTipsBgColor = Color(0xFFD81306),
        adVipBannerPTitleColor = Color(0xFF333333),
        adVipBannerPSubTitleColor = Color(0xFF999999),
        adVipBannerPPriceColor = Color(0xFF9A5D11),
        adVipBannerBTitleColor = Color(0xFF000000),
        gameReserveTitle = Color(0xFF222222),
        adGameCheckInBgColor = Color(0XFFF0F0F0),
        storeHaoDianColor = Color(0xFFE0B584),       // 好店标 日间 #E0B584
        storeRGrey30Color = Color(0x4D000000),        // R标 日间 #000000 30%
        storeRGrey55Color = Color(0x8C000000),        // R标 日间 #000000 55%
        gameCalendarTitle = Color(0xFF333333),    // 游戏日历标题 - 日间
        gameCalendarDesc = Color(0xFF999999),     // 游戏日历描述 - 日间
        gameCalendarTagBg = Color(0xFFE69100),    // 游戏日历标签背景 - 日间 #E69100
        gameCalendarCardShadow = Color(0x40B3B3B3), // 游戏日历卡片阴影 - 日间 #B3B3B3 25%
        gameCalendarAvatarMask = Color.Transparent, // 游戏日历头像遮罩 - 日间透明
        game = GameColorScheme(
            reserveTitle = Color(0xFF222222),
            checkInBgColor = Color(0XFFF0F0F0),
            calendarTitle = Color(0xFF333333),    // 游戏日历标题 - 日间
            calendarDesc = Color(0xFF999999),     // 游戏日历描述 - 日间
            calendarTagBg = Color(0xFFE69100),    // 游戏日历标签背景 - 日间 #E69100
            calendarCardShadow = Color(0x40B3B3B3), // 游戏日历卡片阴影 - 日间 #B3B3B3 25%
            calendarAvatarMask = Color.Transparent, // 游戏日历头像遮罩 - 日间透明
            categoryTabV2DescColor = Color(0xFF666666),
            categoryTabV2MetaColor = Color(0xFFF8A33B),
            feedCardShadow = Color(0x40B3B3B3),
            calendarBorder = Color(0xC2FFFFFF),
            hangCardTitle = Color(0xFF5C5C5C),
            feedCardTitle = Color(0xFF333333),
            feedCardBg = Color(0xFFFFFFFF),
        ),
    ),

    sport = SportColorScheme(
        textPrimary = Color.White,
        reservedBtnBg = Color(255, 255, 255, 26),
        reservedBtnText = Color(255, 255, 255, 77),
        textSecondary = Color(255, 255, 255, 128),
        divider = Color(255, 255, 255, 31),
        endedBtnBg = Color(255, 255, 255, 20),
        highlightBtnBg = Color(51, 119, 255, 77),
        liveRed = Color(0xFFFF0055),
    ),

    user = UserColorScheme(
        historyEditBtnDisabled = Color(0xFFB2B2B2),
        messageDivider = Color(0xFFF0F0F0),
    ),
)

val DarkColorScheme = ColorScheme(
    bNormal = Color(0xFF3377FF),
    bLight = Color(0xFF273240),
    bMiddle = Color(0xFF7780D9),
    bGray = Color(0xFF223F62),
    redNormal = Color(0xFFD90048),
    orangeNormal = Color(0xFFd97500),
    redBg = Color(0xFFB31005),
    assistantText = Color(0xFF696969),
    blueNormal = Color(0xFF3377FF),
    bgContentPreference = Color(0xFF242629),
    backIconColor = Color(0xFFFFFFFF),
    brownNormal = Color(0xFFC44F1F),
    purpleNormal = Color(0xFF7642F5), // 设计Token: brand_primary (夜间)
    sponsorSelectBg = Color(0xFF273240),
    t1 = Color(0xFFFFFFFF),          // 设计Token: text_primary (夜间)
    t2 = Color(0xFFA9A9A9),          // 设计Token: text_secondary (夜间)
    t3 = Color(0xFF696969),          // 设计Token: text_tertiary (夜间)
    t6 = Color(0x4D999999),
    t3DarkAlpha04 = Color(0x66696969),
    t4 = Color(0xFFFFFFFF),
    t5 = Color(0xBFFFFFFF),
    tlink = Color(0xFF776BFF),       // 设计Token: text_link (夜间)
    yNormal = Color(0xFFD99B00),
    bgPage = Color(0xFF1F1F23),      // 设计Token: bg_page (夜间)
    audioPodNormalBgColor = Color(0xFF2B2B2B),
    bgBlock = Color(0xFF262626),     // 设计Token: bg_block (夜间)
    panelBgBlock = Color(0xEE121212),
    panelBgPage = Color(0xFF1F1F23),
    bgCard = Color(0xFF2A2A2A),      // 设计Token: bg_card (夜间)
    bgPageGrey = Color(0xFF121212),  // 设计Token: bg_page_grey (夜间)
    bgPageMidGrey = Color(0xFF121212), // 设计Token: bg_page_midgrey (夜间)
    bgBar = Color(0xFF232327),       // 设计Token: bg_bar (夜间)
    bgTopLight = Color(0xFF303035),  // 设计Token: bg_top_light (夜间)
    bgMiddleStandard = Color(0xFF27272B), // 设计Token: bg_middle_standard (夜间)
    tabContainerBg = Color(0x1AF7F7F7), // 设计Token: tab_container_bg (夜间，#F7F7F7 10%)
    tabSelectedBg = Color(0x1AFFFFFF),  // 设计Token: tab_selected_bg (夜间，白色10%)
    assistantBg = Color(0xFF272633),
    assistantBgPurple = Color(0xFF12132B),
    assistantBgGradient = linearGradient(
        colorStops = arrayOf(
            0f to Color(0xFF464488), // 0%
            0.3f to Color(0xFF12132B), // 30%
            0.71f to Color(0xFF12132B), // 71%
            1f to Color(0xFF12132B)  // 100%
        ),
        start = Offset(0.0f, 0f), end = Offset(0f, Float.POSITIVE_INFINITY)
    ),
    bgAssistantSendStart = Brush.verticalGradient(listOf(Color(0xFF12132B), Color(0x0012132B))),
    bgAssistantSendEnd = Brush.verticalGradient(listOf(Color(0x0012132B), Color(0xFF12132B))),
    audioPodSelectBgBrush = Brush.verticalGradient(
        listOf(Color(0xFF273240), Color(0xD41F1F1F)),
        endY = 0.3f
    ),
    audioPodShadowColor = Color(0x1A000000),
    buttonBlock = Color(0xFF2B2B2B),
    bgBottomSheetGrey = Color(0xFF1F1F1F),
    bgSnackBar = Color(0xFF2B2B2B),  // 设计Token: bg_snackbar (夜间)
    lineFine = Color(0x1AFFFFFF),    // 设计Token: line_standard (夜间)
    lineStandard = Color(0x1AFFFFFF), // 设计Token: line_standard 10%透明度白色 (夜间)
    lineWide = Color(0xFF121212),
    lineStroke = Color(0xFF303030),
    lineWideVideo = Color(0xFF121212),
    lineInside = Color(0xFF3D3D3D),
    lineLight = Color(0x0DFFFFFF),   // 设计Token: line_light (夜间)
    transparent = Color(0x00FFFFFF),
    fillPrimary = Color(0x1AFFFFFF),   // 设计Token: fill_primary (夜间)
    fillSecondary = Color(0x33FFFFFF), // 设计Token: fill_secondary (夜间)
    fillPurple = Color(0x33776BFF),    // 设计Token: fill_purple (夜间)
    btnPrimaryDefault = Color(0xFF7642F5),   // 设计Token: btn_primary_default (夜间)
    btnPrimaryDisable = Color(0x807642F5),   // 设计Token: btn_primary_disable (夜间)
    btnSecondaryDefault = Color(0x0DFFFFFF), // 设计Token: btn_secondary_default (夜间)
    btnLightBrand = Color(0xFFECEBFF),       // 设计Token: btn_lightbrand_default (夜间)
    btnTertiary = Color(0x0DFFFFFF),         // 设计Token: btn_tertiary_default (夜间)
    watchHistoryBtnBgColor = Color(0x1AFFFFFF),
    fbError = Color(0xFFE6574A),     // 设计Token: fb_error (夜间)
    fbCorrect = Color(0xFF57BE6A),   // 设计Token: fb_correct (夜间)
    mask20 = Color(0x33000000),      // 设计Token: mask_20 (夜间)
    mask75 = Color(0xBF000000),      // 设计Token: mask_75 (夜间)
    textLinkBlue = Color(0xFF7A95CC), // 设计Token: text_link_blue (夜间)
    shadow50 = Color.Black.changeAlpha(0.5f),
    shadow40 = Color(0x66000000),
    shadow25 = Color(0x40000000),
    shadow6 = Color(0x0F000000),
    shadowImage = Brush.verticalGradient(
        colors = listOf(
            Color(0x99303030), Color(0xAA303030), Color(0xFF262626)
        )
    ),
    closeBackground = Color(0x0AFFFFFF),
    white70 = Color(0xB2FFFFFF),
    white80 = Color(0xCCFFFFFF),
    hotAskMore = Color(0xFF53586B),
    verticalShadow = Brush.verticalGradient(colors = listOf(Color(0x1A1F1F1F), Color(0x001F1F1F))),
    adMiniGameBg = Color(0x99333333),
    sponsorCardBg = Brush.verticalGradient(colors = listOf(Color(0x14D90048), Color(0x001F1F1F))),
    sponsorProcessBg = Color(0xFF332929),
    whiteText = Color(0xFFD9D9D9),
    paymentColorScheme = DarkPaymentColorScheme,
    collectionHeaderIconGradientColor1 = Color(0xFF444444),
    collectionHeaderIconGradientColor2 = Color(0x66444444),

    greenNormal = Color(0xFF008557),

    // 签到相关颜色 - 夜间
    checkInBgGradientStart = Color(0xFF3E2208),
    checkInBgGradientEnd = Color(0xFF1F1F1F),
    checkInContentBgGradientStart = Color(0xFF2B1005),
    checkInContentBgGradientEnd = Color(0xFF231A16),
    checkInTitleText = Color(0xFFD9D9D9),
    checkInAccentText = Color(0xFFC26A46),
    checkInButtonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFEA755),
            Color(0xFFFF7658),
            Color(0xFFFF5934)
        ),
        start = Offset(0f, 0.5f),
        end = Offset(1f, 0.5f)
    ),
    checkInDashedLine = Color(0xFFEDB8AB),
    checkInCoinText = Color(0xFFB24D04),
    checkInCoinTextChecked = Color(0xFF333333),
    picDefaultColor = Color(0xFF1A1B1C),
    mask50 = Color(0x80000000),
    mask60 = Color(0x99000000),
    bgGameCard = Color(0xFF2A2A2A),
    oran70 = Color(0xFF3617B3),
    bgGameShadow = Color(0x0F666666),
    audioReadTitleColor = Color(0xFF696969),
    rNormal = Color(0xFFF02D65),      // 设计Token: brand_secondary (夜间)
    aigc = AigcColorScheme(
        prompt = Color(0xFF7780D9),
        prompt05 = Color(0x0D7780D9),
        prompt25 = Color(0x407780D9),
        bg = Color(0xFF121212),
        bgSendStart = Brush.verticalGradient(listOf(Color(0xFF121212), Color(0x00121212))),
        bgSendEnd = Brush.verticalGradient(listOf(Color(0x00121212), Color(0xFF121212))),
        lineCard = Color(0x9999994D),
        qaText = Color(0xFF505DE5),
        purple = Color(0xFF505DE5),
        purpleBg12 = Color(0x1F5564FF),
        purpleButonBg = Color(0xFF5564FF),
        stroke = Color(0xFF2F2D3D),
        entryStart = Color(0xFF26222C),
        entryEnd = Color(0xFF22262C),
        streamStart = Color(0xFF102E16),
        streamEnd = Color(0xFF121212),
        bottomBarBorder = Color(0xFF45AD60),
        bottomBarVoiceCancelBorder = Color(0x66FF0055),
        bottomBarBottomMask = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1F1F1F),
                Color(0x4D1B2E26)
            )
        ),
        bottomBarTopMask = Brush.verticalGradient(
            colors = listOf(
                Color(0x00000000),
                Color(0x00000000)
            )
        ),
        bottomBarShadowSpot = Color(0xFF111111),
        bottomBarShadowAmbient = Color(0xFF111111),
        yuanbaoGreen = Color(0xFF45AD60),
        yuanbaoGreen05 = Color(0x0D45AD60),
        yuanbaoGreenBg = Color(0xFF15331C),
        posterGrayBg = Color(0xFFF6F6F6),  // 对话列表海报灰色背景
        posterBlueBg = Color(0xFF167BFF),   // 选中文本渐变海报蓝色背景
        digitalBgMask = Brush.verticalGradient(
            colors = listOf(
                Color(0x00123338),
                Color(0x66123338),
                Color(0xCC123338),
                Color(0xFF10393F),
                Color(0xFF0D3035)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
        )
    ),

    ad = AdColorScheme(
        adMidArticleGameScoreColor = Color(0xFFD96A35),
        adMultiImageBgColor = Color(0xFF8A8A8A),
        adVipBannerBgColor = Color(0xFFD0C6AF),
        adVipBannerTipsBgColor = Color(0xFFA32618),
        adVipBannerPTitleColor = Color(0xFF2B2B2B),
        adVipBannerPSubTitleColor = Color(0xFF828282),
        adVipBannerPPriceColor = Color(0xFF834F0E),
        adVipBannerBTitleColor = Color(0xFFD9D9D9),
        gameReserveTitle = Color(0xFFFFFFFF),
        adGameCheckInBgColor = Color(0xFF262626),
        storeHaoDianColor = Color(0xFFBA966E),       // 好店标 夜间 #BA966E
        storeRGrey30Color = Color(0x4DFFFFFF),        // R标 夜间 #FFFFFF 30%
        storeRGrey55Color = Color(0x80FFFFFF),        // R标 夜间 #FFFFFF 50%
        gameCalendarTitle = Color(0xFFD9D9D9),    // 游戏日历标题 - 夜间
        gameCalendarDesc = Color(0xFF5C5C5C),     // 游戏日历描述 - 夜间
        gameCalendarTagBg = Color(0xFFB97700),    // 游戏日历标签背景 - 夜间 #E69100 叠加 20% 黑色
        gameCalendarCardShadow = Color(0x401F1F1F), // 游戏日历卡片阴影 - 夜间 #1F1F1F 25%
        gameCalendarAvatarMask = Color.Black.copy(alpha = 0.2f), // 游戏日历头像遮罩 - 夜间
        game = GameColorScheme(
            reserveTitle = Color(0xFFFFFFFF),
            checkInBgColor = Color(0xFF262626),
            calendarTitle = Color(0xFFD9D9D9),    // 游戏日历标题 - 夜间
            calendarDesc = Color(0xFF5C5C5C),     // 游戏日历描述 - 夜间
            calendarTagBg = Color(0xFFB97700),    // 游戏日历标签背景 - 夜间 #E69100 叠加 20% 黑色
            calendarCardShadow = Color(0x401F1F1F), // 游戏日历卡片阴影 - 夜间 #1F1F1F 25%
            calendarAvatarMask = Color.Black.copy(alpha = 0.2f), // 游戏日历头像遮罩 - 夜间
            categoryTabV2DescColor = Color(0xFFA9A9A9),
            categoryTabV2MetaColor = Color(0xFFD97500),
            feedCardShadow = Color(0x40000000),
            calendarBorder = Color(0x331F1F1F),
            hangCardTitle = Color(0xFF696969),
            feedCardTitle = Color(0xFFD9D9D9),
            feedCardBg = Color(0xFF262626),
        ),
    ),

    sport = SportColorScheme(
        textPrimary = Color.White,
        reservedBtnBg = Color(255, 255, 255, 26),
        reservedBtnText = Color(255, 255, 255, 77),
        textSecondary = Color(255, 255, 255, 128),
        divider = Color(255, 255, 255, 31),
        endedBtnBg = Color(255, 255, 255, 20),
        highlightBtnBg = Color(51, 119, 255, 77),
        liveRed = Color(0xFFFF0055),
    ),

    user = UserColorScheme(
        historyEditBtnDisabled = Color(0xFF4C4C4C),
        messageDivider = Color(0xFF262626),
    ),
)