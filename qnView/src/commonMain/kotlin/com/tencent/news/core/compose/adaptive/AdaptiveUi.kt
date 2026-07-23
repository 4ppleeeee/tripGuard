package com.tencent.news.core.compose.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.news.core.compose.platform.pageViewHeight
import com.tencent.news.core.compose.platform.pageViewWidth
import com.tencent.news.core.platform.WindowInfo

@Composable
fun AdaptiveUi(
    whenCompat: @Composable () -> Unit,
    whenMedium: @Composable () -> Unit = whenCompat,
    whenExpanded: @Composable () -> Unit = whenMedium,
) {
    val windowType = windowInfo().type
    if (windowType.isExpanded) {
        whenExpanded()
    } else if (windowType.isMedium) {
        whenMedium()
    } else {
        whenCompat()
    }
}

@Composable
fun windowInfo(): WindowInfo {
    val widthDp = pageViewWidth()
    val heightDp = pageViewHeight()
    val density = LocalDensity.current
    return remember(density, widthDp, heightDp) {
        with(density) {
            WindowInfo(widthDp.roundToPx(), heightDp.roundToPx())
        }
    }
}