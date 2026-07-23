package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.painter.Painter

// 如果想自定义一些页面ui，用这个
data class StructPageUICustomize(
    val pageModifier: Modifier? = null,                                   // 整个页面背景
    val contentModifier: Modifier? = null,                                // 内容区背景（pager区域）
    val mainContentBottomPadding: Float = 0f,                             // 页面底边距
    val errorImagePainterProvider: (@Composable () -> Painter)? = null,   // 自定义错误页面图片
    val enableRootPullRefresh: Boolean = false,                           // 是否开启根列表下拉刷新
    val onChannelSwitchIntercept: (Int) -> Boolean = { false },           // 频道切换前拦截，返回 true 表示阻止切换
    val disableChannelSwipe: Boolean = false,                             // 是否禁用频道左右滑动
)