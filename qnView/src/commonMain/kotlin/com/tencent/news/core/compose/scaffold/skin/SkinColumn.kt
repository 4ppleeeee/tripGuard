package com.tencent.news.core.compose.scaffold.skin

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.modifiers.backgroundColor
import com.tencent.news.core.compose.scaffold.modifiers.borderRadius
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.scaffold.theme.QnSkin
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.vm.IFeedsVMItemStub

@Composable
fun IFeedsVMItemStub.BuildSkinColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) = (this as IListItem).BuildSkinColumn(modifier, verticalArrangement, horizontalAlignment, content)

@Composable
fun IListItem.BuildSkinColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    StructSkinItemColumn(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        openHeaderRadius = ctxDto.isSectionHeader,
        openFooterRadius = ctxDto.isSectionFooter,
        content = content
    )
}

/**
 * cell的处理皮肤的外层容器Column
 * 功能：有皮肤的情况下，判断是section的头尾会加上圆角，同时替换背景色
 */
@Composable
fun StructSkinItemColumn(
    modifier: Modifier,
    skinBg: Color = QnColor.bgPage,
    openHeaderRadius: Boolean,
    openFooterRadius: Boolean,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    val hasSkin = QnSkin != null
    if (hasSkin) {
        StructEventItemColumnWithSkin(
            modifier = modifier,
            background = skinBg,
            openHeaderRadius = openHeaderRadius,
            openFooterRadius = openFooterRadius,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            content()
        }
    } else {
        StructEventItemColumnWithoutSkin(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) { content() }
    }
}

@Composable
private fun StructEventItemColumnWithSkin(
    modifier: Modifier,
    background: Color,
    openHeaderRadius: Boolean,
    openFooterRadius: Boolean,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.borderRadius(
            topLeft = if (openHeaderRadius) 4.dp else 0.dp,
            topRight = if (openHeaderRadius) 4.dp else 0.dp,
            bottomLeft = if (openFooterRadius) 4.dp else 0.dp,
            bottomRight = if (openFooterRadius) 4.dp else 0.dp
        ).backgroundColor(background),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        content()
    }
}

@Composable
private fun StructEventItemColumnWithoutSkin(
    modifier: Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        content()
    }
}