package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.page.model.LayersWidget
import com.tencent.news.core.service.ViewService

// 给子tab内嵌专用的浮层挂件
@Composable
internal fun SubPageLayerView(widget: LayersWidget?) {
    val layerWidgets = widget?.getSubPageWidgets()
    if (layerWidgets != null) {
        Box(Modifier.Companion.fillMaxSize()) {
            layerWidgets.forEach { widget ->
                ViewService.layer.Build(this, widget)
            }
        }
    }
}