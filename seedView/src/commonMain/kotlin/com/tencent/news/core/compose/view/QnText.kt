package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.material3.LocalTextStyle
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.resources.Font
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.alpha
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.TextLayoutResult
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.font.toFontFamily
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.TextUnit
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.platform.LocalIOS375ScaleRatio
import com.tencent.news.core.compose.platform.TitleDensityScale
import com.tencent.news.core.compose.scaffold.theme.LocalUiMode
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.scaffold.theme.pingFangFamily
import com.tencent.news.core.extension.isFalseOrNull

// 是否响应app字体大小变化
val LocalFontAutoScale = staticCompositionLocalOf { true }

// 与 Text 或者 QnText 不同点
// 1、iOS 默认显示苹方字体
// 2、Medium 字重
// 3、字号只支持18
@Composable
fun QnPingFangT1Text(
    text: String? = null,
    modifier: Modifier = Modifier,
    color: Color? = null,
    maxLines: Int? = 2, // default is Int.MAX_VALUE
) {
    QnText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 18.sp,
        fontFamily = pingFangFamily,
        fontWeight = FontWeight.Medium,
        overflow =  TextOverflow.Ellipsis,
        maxLines = maxLines,
    )
}

@Composable
fun QnText(
    text: String? = null,
    modifier: Modifier = Modifier,
    color: Color? = null,
    fontSize: TextUnit? = null,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: Float? = null,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: Float? = null,
    overflow: TextOverflow? = null, // default is TextOverflow.Ellipsis
    maxLines: Int? = null, // default is Int.MAX_VALUE
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle? = null,
) {

    val finalFontFamily = getFontFamily(style?.fontFamily ?: fontFamily)

    // 在IOS375Scale作用域内，sp值需要除以scaleBy375来抵消density中对字号的放大，目前只简单处理了Text.fontSize
    val ios375Ratio = LocalIOS375ScaleRatio.current
    val safeIOS375Ratio = ios375Ratio.takeIf { it > 0F } ?: 1F
    val needAdjustIOS375 = safeIOS375Ratio != 1F

    val incomingStyle = style ?: LocalTextStyle.current
    val explicitFontSize = fontSize?.takeIf { it != TextUnit.Unspecified }

    // 显式传入fontSize时，优先调整参数fontSize
    val adjustedFontSize = if (needAdjustIOS375 && explicitFontSize != null) {
        explicitFontSize / safeIOS375Ratio
    } else {
        explicitFontSize ?: TextUnit.Unspecified
    }

    // 未显式传fontSize时，补齐style.fontSize的缩放抵消
    val adjustedStyle = if (
        needAdjustIOS375 &&
        explicitFontSize == null &&
        incomingStyle.fontSize != TextUnit.Unspecified
    ) {
        incomingStyle.copy(fontSize = incomingStyle.fontSize / safeIOS375Ratio)
    } else {
        incomingStyle
    }

    Text(
        text = text ?: "",
        modifier = modifier,
        color = color ?: Color.Unspecified,
        fontSize = adjustedFontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = finalFontFamily,
        letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight.takeIf { it?.isNaN().isFalseOrNull() }?.sp ?: TextUnit.Unspecified,
        overflow = overflow ?: TextOverflow.Ellipsis,
        maxLines = maxLines ?: Int.MAX_VALUE,
        onTextLayout = onTextLayout,
        style = adjustedStyle,
    )
}

@Composable
fun getFontFamily(fontFamily: FontFamily?): FontFamily? {
    val fontFamilyName = LocalUiMode.current.textFontFamily
    return fontFamily ?: fontFamilyName?.let { Font(it).toFontFamily() }
}

@Composable
fun QnTitle(
    text: String,
    fontSize: TextUnit = 16.sp,
    color: Color = QNTheme.colorScheme.t1,
    fontWeight: FontWeight = FontWeight.W500,
    modifier: Modifier = Modifier,
    diableScale: Boolean = false
) {
    TitleDensityScale(diableScale) {
        QnText(
            text = text,
            fontSize = fontSize,
            color = color,
            fontWeight = fontWeight,
            modifier = modifier,
        )
    }
}

@Composable
fun QnT1NumberText(
    text: String,
    fontSize: Float = 16f,
    color: Color = QNTheme.colorScheme.t1,
    fontWeight: FontWeight = FontWeight.W500,
    modifier: Modifier = Modifier,
) {
    QnText(
        text = text,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}


// 字号 24，字重 600
@Composable
fun QnBigT1Text(
    text: String,
    fontSize: Float = 24f,
    color: Color = QNTheme.colorScheme.t1,
    fontWeight: FontWeight = FontWeight.W600,
    modifier: Modifier = Modifier,
) {
    QnText(
        text = text,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}


// 字号 16 字重 500
@Composable
fun QnT1Text(
    text: String,
    fontSize: Float = 16f,
    color: Color = QNTheme.colorScheme.t1,
    fontWeight: FontWeight = FontWeight.W500,
    modifier: Modifier = Modifier,
) {
    QnText(
        text = text,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}

// 字号 16，字重 400
@Composable
fun QnT2Text(
    text: String,
    fontSize: Float = 14f,
    color: Color = QNTheme.colorScheme.t2,
    fontWeight: FontWeight = FontWeight.W400,
    modifier: Modifier = Modifier,
) {
    QnText(
        text = text,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}

// 字号 14，字重 400，颜色 t3
@Composable
fun QnT3Text(
    text: String,
    fontSize: Float = 14f,
    color: Color = QNTheme.colorScheme.t3,
    fontWeight: FontWeight = FontWeight.W400,
    modifier: Modifier = Modifier,
) {
    QnText(
        text = text,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}


// 字号 14，字重 400
@Composable
fun QnBlueNormalText(
    text: String,
    fontSize: Float = 14f,
    color: Color = QNTheme.colorScheme.blueNormal,
    fontWeight: FontWeight = FontWeight.W400,
    modifier: Modifier = Modifier,
) {
    QnText(
        text = text,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier,
    )
}

// 可调节透明度的白色 字号 14 字重 500
@Composable
fun QnWhiteText(
    text: String,
    alpha: Float = 1f,
    fontSize: Float = 14f,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.W500,
    modifier: Modifier = Modifier,
) {
    QnText(
        text = text,
        fontSize = fontSize.sp,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier.alpha(alpha),
    )
}
