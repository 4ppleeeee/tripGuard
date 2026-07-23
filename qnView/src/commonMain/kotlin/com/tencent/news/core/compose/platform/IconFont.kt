package com.tencent.news.core.compose.platform

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.semantics.clearAndSetSemantics
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.compose.scaffold.theme.iconFontFamily
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.isHarmonyPlatform


expect fun getIconFontMapping(): Map<String, String>

val iconFontMapping by lazy { getIconFontMapping() }


/**
 * 若需要响应字号设置，不要在TextStyle中设置fontSize，单独设置，否则不要单独设置fontSize，在TextStyle中设置
 */
@Composable
fun QnIconFont(
    name: IconFont,
    textStyle: TextStyle = TextStyle(),
    fontSize: Float? = null,
    modifier: Modifier = Modifier,
//    nativeRef: RefFunc<TextView>? = null,
) {
    QnIconFont(
        name = name.code,
        textStyle = textStyle,
        fontSize = fontSize,
        modifier = modifier,
//        nativeRef = nativeRef
    )
}

@Composable
fun QnIconFont(
    name: String,
    textStyle: TextStyle = TextStyle(),
    fontSize: Float? = null,
//    nativeRef: RefFunc<TextView>? = null,
    modifier: Modifier = Modifier,
) {
    var realName = name
    if (isHarmonyPlatform()) {
        realName = name.replace("-", "_")
    }
    QnText(
        text = iconFontMapping.getOrElse(realName) { realName },
        style = textStyle,
        fontSize = fontSize?.sp,
        fontFamily = iconFontFamily,
        modifier = Modifier.clearAndSetSemantics { }.then(modifier),
//        nativeRef = nativeRef
    )
}

