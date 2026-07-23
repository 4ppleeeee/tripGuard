@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.RowScope
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp


@Composable
fun SpacerHeight(height: Dp) {
    Spacer(Modifier.height(height))
}


@Composable
fun SpacerHeight(height: Float) {
    Spacer(Modifier.height(height.dp))
}


@Composable
fun SpacerWidth(width: Dp) {
    Spacer(Modifier.width(width))
}

@Composable
fun SpacerWidth(width: Float) {
    Spacer(Modifier.width(width.dp))
}

@Composable
fun RowScope.SpacerWeight() {
    Spacer(modifier = Modifier.weight(1f))
}