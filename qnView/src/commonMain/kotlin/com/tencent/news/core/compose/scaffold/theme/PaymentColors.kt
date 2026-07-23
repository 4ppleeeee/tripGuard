package com.tencent.news.core.compose.scaffold.theme

import androidx.compose.runtime.Immutable
import com.tencent.kuikly.compose.ui.graphics.Color

/**
 * 支付相关颜色配置
 */
@Immutable
data class PaymentColorScheme(
    val textColor: Color,
    val textColor2: Color,
    val textBgColor: Color,
    val toastBgColor: Color,
    val cardBg: Color,
    val indicatorLine: Color,
    val columnGiftTopBarTextColor: Color,
    val cardDescBg: Color,
    val cardDescSelectedBg: Color,
    val countdownBg: Color,
    val balanceRedColor: Color,
    val loadingBg: Color,
)

val LightPaymentColorScheme = PaymentColorScheme(
    textColor = Color(0xFF7A4607),
    textColor2 = Color(0xFFF8DEAA),
    textBgColor = Color(0xFFFFF2D8),
    toastBgColor = Color(0xFFFFF2D8),
    cardBg = Color(0xFFFFF2D8),
    indicatorLine = Color(0xFF7A4607),
    columnGiftTopBarTextColor = Color(0xFF834801),
    cardDescBg = Color(0xFFE6E6E6),
    cardDescSelectedBg = Color(0xFFF8DEAA),
    countdownBg = Color(0xFFF8DEAA),
    balanceRedColor = Color(0xFFD81306),
    loadingBg = Color(0xE63D3D3D),
)

val DarkPaymentColorScheme = PaymentColorScheme(
    textColor = Color(0xFFFFD79C),
    textColor2 = Color(0xFFF8DEAA),
    textBgColor = Color(0xFF423A2D),
    toastBgColor = Color(0xFF423A2D),
    cardBg = Color(0xFF423A2D),
    indicatorLine = Color(0xFFFFD79C),
    columnGiftTopBarTextColor = Color(0xFFFFD7A8),
    cardDescBg = Color(0xFF3D3D3D),
    cardDescSelectedBg = Color(0xFF544A39),
    countdownBg = Color(0xFF584E3D),
    balanceRedColor = Color(0xFFB33535),
    loadingBg = Color(0xE63D3D3D),
)

/**
 * 向后兼容的扩展属性，方便业务代码平滑迁移
 * @deprecated 请使用 paymentColorScheme.xxx 代替
 */
val ColorScheme.vipColor: Color get() = paymentColorScheme.textColor
val ColorScheme.paymentTextColor: Color get() = paymentColorScheme.textColor
val ColorScheme.paymentTextColor2: Color get() = paymentColorScheme.textColor2
val ColorScheme.paymentTextBgColor: Color get() = paymentColorScheme.textBgColor
val ColorScheme.paymentToastBgColor: Color get() = paymentColorScheme.toastBgColor
val ColorScheme.paymentCardBg: Color get() = paymentColorScheme.cardBg
val ColorScheme.paymentIndicatorLine: Color get() = paymentColorScheme.indicatorLine
val ColorScheme.paymentCardDescBg: Color get() = paymentColorScheme.cardDescBg
val ColorScheme.paymentCardDescSelectedBg: Color get() = paymentColorScheme.cardDescSelectedBg
val ColorScheme.paymentCountdownBg: Color get() = paymentColorScheme.countdownBg
val ColorScheme.paymentBalanceRedColor: Color get() = paymentColorScheme.balanceRedColor
val ColorScheme.paymentLoadingBg: Color get() = paymentColorScheme.loadingBg

/**
 * 专栏赠送提示条文字色，对齐 Android golden_cp_vip_11 / night_golden_cp_vip_3。
 */
val ColorScheme.paymentColumnGiftTopBarTextColor: Color get() = paymentColorScheme.columnGiftTopBarTextColor
