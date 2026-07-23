package com.tencent.news.core.compose.scaffold.widgetbtns

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.platform.statusBarHeight

@Composable
fun StatusBarSpacer() {
    // 撑开状态栏
    Spacer(Modifier.fillMaxWidth().height(statusBarHeight()))
}