package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.BoxScope
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.view.SpacerHeight
import com.tencent.news.core.compose.view.SpacerWidth
import com.tencent.news.core.extension.takeIfNotEmpty
import com.tencent.news.core.page.model.BtnListWidget
import com.tencent.news.core.service.ViewService

@Composable
fun BoxScope.BtnListView(widget: BtnListWidget) {
    val btnList = widget.btnList.takeIfNotEmpty() ?: return

    val alignment = when (widget.align) {
        BtnListWidget.Align.BOTTOM_CENTER -> Alignment.BottomCenter
        BtnListWidget.Align.BOTTOM_START -> Alignment.BottomStart
        BtnListWidget.Align.BOTTOM_END -> Alignment.BottomEnd
    }

    // 通用的挂件位置：距离底部60dp，故意不开放控制字段的；
    // 如果有不一样的case，优先与设计沟通，对齐位置规范
    Box(Modifier.align(alignment).margin(start = 16.dp, end = 16.dp, bottom = 60.dp)) {
        if (widget.ui.isVertical) {
            Column {
                btnList.forEachIndexed { index, btnWidget ->
                    ViewService.btn.Build(btnWidget)

                    if (index != btnList.size - 1) {
                        SpacerHeight(widget.ui.space.dp)
                    }
                }
            }
        } else {
            Row {
                btnList.forEachIndexed { index, btnWidget ->
                    ViewService.btn.Build(btnWidget)

                    if (index != btnList.size - 1) {
                        SpacerWidth(widget.ui.space.dp)
                    }
                }
            }
        }
    }

}