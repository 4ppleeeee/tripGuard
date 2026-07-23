package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.border
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.wrapContentSize
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.semantics.clearAndSetSemantics
import com.tencent.kuikly.compose.ui.semantics.contentDescription
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.app.constants.DensityScaleGradient
import com.tencent.news.core.app.constants.IIconFont
import com.tencent.news.core.compose.platform.MaxDensityScale
import com.tencent.news.core.compose.platform.QnIconFont
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha
import com.tencent.news.core.compose.scaffold.modifiers.dtElement
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.page.model.ITextIconBtnVM
import com.tencent.news.core.page.model.StructSize

/**
 * 带文字的图标按钮组件
 * 支持响应头部折叠状态
 */
@Composable
fun TextIconBtn(viewModel: ITextIconBtnVM?) {
    val vm = viewModel ?: return
    val showRedDot by vm.hasRedDot.collectAsState()
    val isHeaderCollapsed = currentTitleBarTheme.isHeaderCollapsed
    val shouldShowText = vm.shouldShowText(isHeaderCollapsed)
    val shouldShowBackground = vm.shouldShowBackground(isHeaderCollapsed)
    MaxDensityScale(DensityScaleGradient.L2) {
        Box(
            modifier = Modifier.wrapContentSize()
        ) {
            Column(
                modifier = Modifier
                    .dtElement(
                        elementId = vm.dtElementId,
                        enableExposure = true,
                        elementParams = vm.dtParams
                    )
                    .clearAndSetSemantics {
                        contentDescription = vm.contentDescription ?: ""
                    }
                    .padding(8.dp)
                    .then(iconBgModifier(shouldShowBackground, vm.backgroundCornerRadius))
                    .clickable { vm.onClick() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (shouldShowText) {
                    TextWithIcon(vm.displayText, vm.iconFont, vm.size)
                } else {
                    TextIconFont(vm.iconFont, vm.size)
                }
            }

            if (showRedDot) {
                IconRedDot()
            }
        }
    }
}

/**
 * 文字+图标组合
 */
@Composable
private fun TextWithIcon(text: String, iconFont: IIconFont, size: StructSize) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        QnText(
            text = text,
            color = currentTitleBarTheme.widgetFgColor,
            fontSize = 14.sp,
            lineHeight = 16f
        )
        TextIconFont(iconFont, size)
    }
}

/**
 * 红点指示器
 */
@Composable
private fun BoxScope.IconRedDot() {
    Box(
        modifier = Modifier
            .align(alignment = Alignment.TopEnd)
            .margin(start = 4f.dp, top = 8f.dp)
            .size(8f.dp)
            .background(QNTheme.colorScheme.redNormal)
            .clip(RoundedCornerShape(4.dp))
    )
}

/**
 * 图标背景样式
 */
@Composable
private fun iconBgModifier(showBackground: Boolean, cornerRadius: Float): Modifier {
    return if (showBackground) {
        Modifier
            .border(
                width = 0.5.dp,
                color = QNTheme.colorScheme.bgPage.changeAlpha(alpha = 0.9f),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .background(
                color = QNTheme.colorScheme.bgPage.changeAlpha(alpha = 0.5f),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    } else {
        Modifier
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            )
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            )
    }
}

@Composable
private fun TextIconFont(iconFont: IIconFont, size: StructSize) {
    QnIconFont(
        name = iconFont,
        textStyle = TextStyle(
            color = currentTitleBarTheme.widgetFgColor,
        ),
        fontSize = size.width.sp.value,
        modifier = Modifier.size(size.width.dp)
    )
}