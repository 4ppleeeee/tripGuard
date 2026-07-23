package com.tencent.news.core.compose.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.tencent.kuikly.compose.ui.platform.LocalConfiguration

@Stable
@Composable
actual fun getWindowHeightInDp(): Float = LocalConfiguration.current.activityHeight