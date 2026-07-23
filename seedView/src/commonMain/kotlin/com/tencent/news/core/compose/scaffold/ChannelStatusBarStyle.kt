package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import com.tencent.news.core.compose.scaffold.registry.CollectPageOnResume
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.platform.api.StatusBarStyle
import com.tencent.news.core.platform.api.statusBarController

@Composable
internal fun ChannelWidget.ApplyStatusBarStyleEffect() {
    CollectPageOnResume(this) {
        when (statusBarStyle) {
            StatusBarStyle.LIGHT -> statusBarController.setWhiteBar()
            StatusBarStyle.DARK -> statusBarController.setBlackBar()
            null -> Unit
        }
    }
}
