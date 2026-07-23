package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.Canvas
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Offset
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.StrokeCap
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.theme.QNTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QnIOSSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = QNTheme.colorScheme.t3,
    dotCount: Int = 12,
    dotWidth: Dp = 3.dp,
    dotHeight: Dp = 12.dp,
    fps: Int = 12, // 控制帧率
    durationSeconds: Float = 1f
) {
    val validFps = fps.coerceAtLeast(1)
    val validDurationSeconds = durationSeconds.coerceAtLeast(0.1f)
    var angle by remember { mutableStateOf(0f) }
    val frameDelay = (1000f / validFps).toLong()
    val angleStep = 360f / (validFps * validDurationSeconds)

    LaunchedEffect(validFps, validDurationSeconds) {
        while (true) {
            angle += angleStep
            if (angle >= 360f) angle -= 360f
            delay(frameDelay)
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = size.toPx() / 2
            val center = Offset(radius, radius)
            for (i in 0 until dotCount) {
                // 关键：每个点的角度加上整体旋转角度
                val theta = ((360.0 / dotCount * i) - 90 + angle) * (PI / 180)
                val x = center.x + (radius - dotHeight.toPx()) * cos(theta)
                val y = center.y + (radius - dotHeight.toPx()) * sin(theta)
                val alpha = (i + 1).toFloat() / dotCount
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(x.toFloat(), y.toFloat()),
                    end = Offset(
                        (center.x + (radius - dotWidth.toPx()) * cos(theta)).toFloat(),
                        (center.y + (radius - dotWidth.toPx()) * sin(theta)).toFloat()
                    ),
                    strokeWidth = dotWidth.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}