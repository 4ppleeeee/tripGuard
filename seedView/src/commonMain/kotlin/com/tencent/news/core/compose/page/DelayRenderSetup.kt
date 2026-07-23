package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.ChannelWidget
import com.tencent.news.core.page.model.DelayRenderType
import kotlinx.coroutines.delay

@Composable
fun ChannelWidget.DelayRenderEffect(isSelected: Boolean, delayRenderType: DelayRenderType?) {
    // 兄弟 tab：用户选中该 tab 后永久关闭延迟渲染，避免再次切回时重复显示 loading。
    LaunchedEffect(isSelected, delayRenderType) {
        if (isSelected && delayRenderType == DelayRenderType.SiblingTab) {
            status.delayRenderType.value = null
            NewsChannelLog.fileLog("DelayRender", "canceled: $delayRenderType")
        }
    }

    LaunchedEffect(delayRenderType) {
        if (delayRenderType != null) {
            NewsChannelLog.fileLog("DelayRender", "type: $delayRenderType")
        }
    }

    // 主 tab 懒加载：默认已选中也先只发请求，等弹窗入场动画结束后再放行列表渲染。
    LaunchedEffect(delayRenderType) {
        val mainTabDelay = delayRenderType as? DelayRenderType.MainTab
        if (mainTabDelay != null) {
            delay(mainTabDelay.delayMs)
            status.delayRenderType.value = null
            NewsChannelLog.fileLog("DelayRender", "canceled: $delayRenderType")
        }
    }
}