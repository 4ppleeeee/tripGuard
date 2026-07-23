package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.tencent.kuikly.compose.animation.AnimatedVisibility
import com.tencent.kuikly.compose.animation.fadeIn
import com.tencent.kuikly.compose.animation.fadeOut
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.news.core.compose.scaffold.modifiers.changeAlpha
import com.tencent.news.core.compose.view.extension.dialogPenetrateClickEvent

@Composable
fun BottomAnimatedDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    key(Any()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.changeAlpha(0.4f))
                    .clickable(onClick = onDismiss)
                    .dialogPenetrateClickEvent(true)
            ) {
                content(Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

