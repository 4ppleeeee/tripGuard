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
)