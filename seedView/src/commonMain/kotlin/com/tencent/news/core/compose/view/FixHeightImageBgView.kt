package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.coil3.rememberAsyncImagePainter
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.theme.WithThemeUrl
import com.tencent.news.core.page.model.FixHeightImageBgWidget

// 【通用】背景图组件
@Composable
fun FixHeightImageBgView(widget: FixHeightImageBgWidget) {
    WithThemeUrl(
        lightUrl = widget.dayUrl,
        darkUrl = widget.nightUrl
    ) { url ->
        QnImage(
            modifier = Modifier.fillMaxWidth().height(widget.fixHeight.dp),
            painter = rememberAsyncImagePainter(url),
            contentDescription = "背景图",

            // 由于组件是固定高度的，这里图片可能形变撑满容器；
            // 不要用crop，在不适配的屏幕上截断后，可能看到背景图底部有个分割线
            contentScale = ContentScale.FillBounds,
        )
    }
}