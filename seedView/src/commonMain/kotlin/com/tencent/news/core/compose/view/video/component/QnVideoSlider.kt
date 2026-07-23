package com.tencent.news.core.compose.view.video.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.material3.ExperimentalMaterial3Api
import com.tencent.kuikly.compose.material3.Slider
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.view.video.QnVideoProgressData
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QnVideoSlider(
    onPositionChange: (Long) -> Unit,
    progressFlow: MutableSharedFlow<QnVideoProgressData?>,
    modifier: Modifier = Modifier,
    sliderModifier: Modifier = Modifier,
) {
    val progressData by progressFlow.collectAsState(initial = null)

    val currentPosition = progressData?.position ?: 0L
    val duration = progressData?.duration ?: 0L
    val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f

    Column(modifier = modifier) {
        Slider(
            value = progress,
            onValueChange = { newProgress ->
                val newPosition = (newProgress * duration).toLong()
                onPositionChange(newPosition)
            },
            enabled = true,
            modifier = sliderModifier,
            thumb = {},
            track = { _ ->
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth(progress)
                            .background(Color.White.copy(alpha = 0.5f))
                    )
                }
            },
            valueRange = 0f..1f
        )
    }
}