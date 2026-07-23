package com.tencent.news.core.compose.scaffold.modifiers

import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.LayoutDirection
import com.tencent.kuikly.compose.ui.unit.dp

fun Modifier.padding2(hor: Float = 0F, ver: Float = 0F): Modifier {
    return this.padding(start = hor, top = ver, end = hor, bottom = ver)
}

fun Modifier.padding2(hor: Dp = 0.dp, ver: Dp = 0.dp): Modifier {
    return this.padding(start = hor, top = ver, end = hor, bottom = ver)
}

fun Modifier.padding(paddingValues: PaddingValues): Modifier {
    return this.padding(
        start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
        end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
        top = paddingValues.calculateTopPadding(),
        bottom = paddingValues.calculateBottomPadding(),
    )
}