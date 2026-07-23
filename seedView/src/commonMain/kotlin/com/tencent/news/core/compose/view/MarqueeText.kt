package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.animation.core.LinearEasing
import com.tencent.kuikly.compose.animation.core.animateIntAsState
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxHeight
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.foundation.layout.wrapContentHeight
import com.tencent.kuikly.compose.foundation.lazy.LazyRow
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.semantics.contentDescription
import com.tencent.kuikly.compose.ui.semantics.semantics
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.scaffold.modifiers.onSizeChangedDp
import kotlinx.coroutines.delay

/**
 * 自动滚动文本组件/跑马灯
 * 当文本内容超出可显示区域时，会自动水平滚动显示全部内容
 * 滚动一次后停止
 */
@Deprecated("优先使用MarqueeTextV2")
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Float = 16f, // 字体大小
    textColor: Color = Color.Black, // 字体颜色
    backgroundColor: Color = Color.Transparent,
    speed: Int = 8000, // 控制跑马灯速度的参数，可以调整
    startDelay: Long = 300, // 动画开始前的延迟时间(毫秒),给用户看到开头几个字的机会
    autoScroll: Boolean = true, // 是否自动滚动
) {
    var offsetX by remember { mutableStateOf(0f) }
    var textWidth by remember { mutableStateOf(0f) }
    var containerWidth by remember { mutableStateOf(0f) }
    var shouldScroll by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }

    // 当text变化时，重置动画状态
    LaunchedEffect(text) {
        // 文本变化时立即重置位置
        offsetX = 0f
        isAnimating = false
        // 需要重新评估文本尺寸和容器尺寸
        shouldScroll = false
    }

    LaunchedEffect(textWidth, containerWidth) {
        // 更新是否需要滚动
        shouldScroll = textWidth > containerWidth
    }

    val animatedOffsetX by animateIntAsState(
        targetValue = if (isAnimating) (textWidth - containerWidth).toInt() else 0,
        animationSpec = tween(
            durationMillis = speed, // 动画时长
            easing = LinearEasing
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .onSizeChangedDp { size ->
                containerWidth = size.width.toFloat()
                // 检查是否需要滚动
                shouldScroll = textWidth > containerWidth
            }
            .semantics {
                contentDescription = text
            },
        contentAlignment = Alignment.CenterStart
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            userScrollEnabled = false
        ) {
            item {
                QnText(
                    text = text,
                    color = textColor,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .offset(
                            x = (if (shouldScroll) -animatedOffsetX else 0).dp,
                            y = 0.dp
                        )
                        .fillMaxHeight()
                        .onSizeChangedDp { size ->
                            textWidth = size.width.toFloat()
                        }
                )
            }
        }
    }

    LaunchedEffect(shouldScroll, textWidth, containerWidth, autoScroll) {
        // 只有当文本宽度大于容器宽度且允许自动滚动时才启动动画
        if (shouldScroll && textWidth > 0 && containerWidth > 0 && !isAnimating && autoScroll) {
            delay(startDelay) // 使用参数化的延迟时间
            isAnimating = true
        }
    }
}