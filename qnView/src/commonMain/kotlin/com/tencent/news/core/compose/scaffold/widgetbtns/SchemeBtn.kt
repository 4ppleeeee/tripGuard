package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.semantics.clearAndSetSemantics
import com.tencent.kuikly.compose.ui.semantics.contentDescription
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.compose.platform.QnIconFont
import com.tencent.news.core.compose.scaffold.modifiers.QnImageCompat
import com.tencent.news.core.compose.scaffold.modifiers.dtElement
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.scaffold.theme.WithThemeUrl
import com.tencent.news.core.compose.utils.parseColor
import com.tencent.news.core.compose.view.QnLottie
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.compose.view.extension.setClickVM
import com.tencent.news.core.page.model.SchemeBtnWidget
import com.tencent.news.core.page.model.StructImage
import com.tencent.news.core.page.model.StructLottie
import com.tencent.news.core.page.model.StructSize
import com.tencent.news.core.page.model.StructText
import com.tencent.news.core.page.model.StructTextAlignment
import com.tencent.news.core.platform.api.appRouter
import kotlinx.coroutines.launch

@Composable
fun SchemeBtn(widget: SchemeBtnWidget?) {
    val data = widget?.data ?: return

    val scope = rememberCoroutineScope()
    val size = data.icon?.size ?: StructSize.TOP_ICON
    val textAlign = data.textAlignment
    val forceUseCustomTextColor = data.forceUseCustomTextColor
    val clickVM = data.clickVM
    var showRedDot by remember { mutableStateOf(data.hasRedDot) }
    Box(modifier = Modifier.wrapContentSize()) {
        Column(
            modifier = Modifier
                .dtElement(
                    elementId = data.dtEid,
                    enableExposure = data.dtEnableExposure,
                    elementParams = data.dtElementParams
                )
                .clearAndSetSemantics { contentDescription = data.contentDescription ?: "" }
                .clickable {
                    scope.launch {
                        appRouter().to(LocalKmmContext, data.jumpScheme)
                    }
                    showRedDot = false
                }
                .setClickVM(clickVM) {
                    showRedDot = false
                }  // 若clickVM不为空，自动覆盖上面的clickable
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (textAlign == StructTextAlignment.TOP) {
                SchemeText(
                    btnText = data.btnText,
                    forceUseCustomTextColor = forceUseCustomTextColor
                )
            }

            SchemeIconFont(data.iconFont, size)

            SchemeIcon(data.icon, size)

            SchemeLottie(data.lottie, size)

            if (textAlign == StructTextAlignment.BOTTOM) {
                SchemeText(
                    modifier = Modifier.margin(top = 5f.dp),
                    btnText = data.btnText,
                    forceUseCustomTextColor = forceUseCustomTextColor
                )
            }
            // todo 补充其他case
        }
        if (showRedDot) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .margin(start = 4f.dp, top = 8f.dp)
                    .size(8f.dp)
                    .background(QNTheme.colorScheme.redNormal)
                    .clip(
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }

}

@Composable
private fun SchemeIconFont(iconFont: IconFont?, size: StructSize) {
    iconFont ?: return
    QnIconFont(
        name = iconFont,
        textStyle = TextStyle(
            color = currentTitleBarTheme.widgetFgColor,
            fontSize = size.width.sp,
        )
    )
}

@Composable
private fun SchemeText(
    modifier: Modifier = Modifier,
    btnText: StructText?,
    forceUseCustomTextColor: Boolean = false
) {
    btnText ?: return
    val color = if (forceUseCustomTextColor) {
        btnText.color.dayColor.parseColor()
    } else {
        currentTitleBarTheme.widgetFgColor
    }
    QnText(
        modifier = modifier,
        text = btnText.text,
        color = color,
        fontSize = btnText.size.width.sp

    )
}

@Composable
private fun SchemeIcon(icon: StructImage?, size: StructSize) {
    icon ?: return
    val normalStyle = icon.normal_style ?: return
    WithThemeUrl(normalStyle.dayUrl, normalStyle.nightUrl) { url: String ->
        QnImageCompat(
            url,
            modifier = Modifier.width(size.width.dp).height(size.height.dp),
            contentScale = ContentScale.Inside
        )
    }
}

@Composable
private fun SchemeLottie(lottie: StructLottie?, size: StructSize) {
    lottie ?: return
    QnLottie(
        modifier = Modifier.width(size.width.dp).height(size.height.dp),
        lottie = lottie
    )
}
