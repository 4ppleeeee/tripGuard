package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.scaffold.registry.LocalStructChannelOffset

// 用于适配：子tab中的浮层，元素想要在整个页面范围内居中
@Composable
fun StructCenterLayerContainer(modifier: Modifier, content: @Composable () -> Unit) {
    val channelOffset by LocalStructChannelOffset.current

    Box(
        modifier = modifier.fillMaxSize().offset(y = -channelOffset / 2),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}