package com.tencent.news.core.compose.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.getRealContext
import com.tencent.news.core.platform.api.androidViewLogic

@Stable
@Composable
actual fun getWindowHeightInDp(): Float {
    val windowMetrics = LocalKmmContext.getRealContext()?.resources?.displayMetrics
        ?: return LocalConfiguration.current.activityHeight
    return windowMetrics.heightPixels / windowMetrics.density
}

@Stable
@Composable
actual fun fixedDp(value: Float): Dp {
    val fixedPx = androidViewLogic().dpToPxNoScale(value)
    return with(LocalDensity.current) { fixedPx.toDp() }
}
