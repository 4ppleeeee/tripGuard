package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.modifiers.backgroundColor
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.isDebug

@Composable
fun DebugBtn(text: String, onClick: () -> Unit) {
    if (!isDebug()) {
        return
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .margin(5.dp)
            .backgroundColor(Color.Blue)
            .clickable {
                debugToast("点击了 '$text' 按钮")
                onClick()
            }
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
        )
    }

}