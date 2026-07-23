package com.tencent.news.core.compose.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.getRealContext

@Stable
@Composable
actual fun getWindowHeightInDp(): Float {
    val windowMetrics = LocalKmmContext.getRealContext()?.resources?.displayMetrics
        ?: return LocalConfiguration.current.activityHeight
    return windowMetrics.heightPixels / windowMetrics.density
}