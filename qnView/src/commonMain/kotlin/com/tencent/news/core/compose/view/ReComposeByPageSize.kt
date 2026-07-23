@file:Suppress("FunctionNaming")

package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.tencent.news.core.compose.platform.pageViewHeightValue
import com.tencent.news.core.compose.platform.pageViewWidthValue

// 屏幕尺寸变化时刷新view，普遍配合 AnimatedVisibility 使用
@Composable
fun ReComposeByPageSize(content: @Composable () -> Unit) {
    // 注意要转int，防止float精度波动
    // / 10 * 10 的目的是去掉个位数，防止一些小幅度屏幕尺寸变化导致频繁重组
    val pageViewWidth = pageViewWidthValue().toInt() / 10 * 10
    val pageViewHeight = pageViewHeightValue().toInt() / 10 * 10
    key(pageViewWidth, pageViewHeight) {
        content()
    }
}