package com.tencent.news.core.util

import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.vm.IComposeContext

data class ViewAnchor(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val nativeViewRef: Int = INVALID_NUM,
    val composeContext: IComposeContext? = null
)