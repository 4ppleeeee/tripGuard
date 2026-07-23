package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.text.InlineTextContent
import com.tencent.kuikly.compose.material3.LocalTextStyle
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.text.AnnotatedString
import com.tencent.kuikly.compose.ui.text.TextLayoutResult
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.TextUnit
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.compose_dsl.kuikly.extension.lineSpacing
import com.tencent.news.core.extension.isFalseOrNull

@Composable
fun QnText(
    text: AnnotatedString? = null,
    modifier: Modifier = Modifier,
    color: Color? = null,
    fontSize: TextUnit? = null,
    autoScale: Boolean = true,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: Float? = null,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit? = null,
    lineSpacing: Float? = null,
    softWrap: Boolean = true,
    overflow: TextOverflow? = null, // default is TextOverflow.Ellipsis
    maxLines: Int? = null, // default is Int.MAX_VALUE
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    /* 内联UI占位内容实现图文混排 */
    inlineContent: Map<String, InlineTextContent>? = null,
    style: TextStyle? = null,
) {

    val finalFontFamily = getFontFamily(style?.fontFamily ?: fontFamily)

    Text(
        text = text ?: buildAnnotatedString { },
        modifier = modifier.lineSpacing(lineSpacing),
        color = color ?: Color.Unspecified,
        fontSize = fontSize ?: TextUnit.Unspecified,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = finalFontFamily,
        letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight.takeIf { it?.value?.isNaN().isFalseOrNull() } ?: TextUnit.Unspecified,
        overflow = overflow ?: TextOverflow.Ellipsis,
        maxLines = maxLines ?: Int.MAX_VALUE,
        onTextLayout = onTextLayout ?: {},
        inlineContent = inlineContent ?: emptyMap(),
        style = style ?: LocalTextStyle.current,
        softWrap = softWrap
    )
}