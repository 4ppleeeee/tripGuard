package com.tencent.news.core.compose.view


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import com.tencent.kuikly.compose.animation.core.LinearEasing
import com.tencent.kuikly.compose.animation.core.RepeatMode
import com.tencent.kuikly.compose.animation.core.TargetBasedAnimation
import com.tencent.kuikly.compose.animation.core.VectorConverter
import com.tencent.kuikly.compose.animation.core.infiniteRepeatable
import com.tencent.kuikly.compose.animation.core.tween
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clipToBounds
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.Placeable
import com.tencent.kuikly.compose.ui.layout.SubcomposeLayout
import com.tencent.kuikly.compose.ui.text.TextLayoutResult
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.font.FontFamily
import com.tencent.kuikly.compose.ui.text.font.FontStyle
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextDecoration
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay

/**
 * 自动滚动文本组件/跑马灯
 * 当文本内容超出可显示区域时，会自动水平滚动显示全部内容
 */
@Composable
fun MarqueeTextV2(
    text: String? = null,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    color: Color? = null,
    fontSize: TextUnit? = null,
    autoScale: Boolean = true,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: Float? = null,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: Float? = null,
    lineSpacing: Float? = null,
    overflow: TextOverflow? = null,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle? = null,
) {
    val createText = @Composable { localModifier: Modifier ->
        QnText(
            text = text,
            modifier = textModifier.then(localModifier),
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            maxLines = 1,
            onTextLayout = onTextLayout,
            style = style
        )
    }
    var offset by remember(text) { mutableStateOf(0) }
    val textLayoutInfoState = remember(text) { mutableStateOf<TextLayoutInfo?>(null) }
    LaunchedEffect(
        text,
        textLayoutInfoState.value?.textWidth,
        textLayoutInfoState.value?.containerWidth,
    ) {
        offset = 0
        val textLayoutInfo = textLayoutInfoState.value ?: return@LaunchedEffect
        if (textLayoutInfo.textWidth <= textLayoutInfo.containerWidth) return@LaunchedEffect
        val duration = 7500 * textLayoutInfo.textWidth / textLayoutInfo.containerWidth
        val delay = 1000L

        do {
            val animation = TargetBasedAnimation(
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = duration,
                        delayMillis = 1000,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                typeConverter = Int.VectorConverter,
                initialValue = 0,
                targetValue = -textLayoutInfo.textWidth
            )
            val startTime = withFrameNanos { it }
            do {
                val playTime = withFrameNanos { it } - startTime
                offset = (animation.getValueFromNanos(playTime))
            } while (!animation.isFinishedFromNanos(playTime))
            delay(delay)
        } while (true)
    }

    SubcomposeLayout(
        modifier = modifier.clipToBounds()
    ) { constraints ->
        val infiniteWidthConstraints = constraints.copy(maxWidth = Int.MAX_VALUE)
        val mainText = subcompose("MainText") {
            createText(Modifier)
        }.first().measure(infiniteWidthConstraints)

        var gradient: Placeable? = null
        var secondPlaceableWithOffset: Pair<Placeable, Int>? = null
        // 滚动判定：
        // 1) 启动阈值（START_SCROLL_TOLERANCE_PX）：当 mainText 宽度接近容器宽度（差距 ≤ 该值）时即触发滚动，
        //    解决 mainText.width 与 constraints.maxWidth 因测量取整、排版裁切等原因相等或仅差 1-2 像素时
        //    视觉上已贴边但 shouldScroll=false 不滚动的问题。
        // 2) 停止阈值（STOP_SCROLL_HYSTERESIS_PX，hysteresis 滞回）：一旦已滚动，需容器比文本宽出该值才允许停止，
        //    避免容器宽度抖动（如 611↔627）导致刚启动的滚动被立即终止。
        val isScrolling = textLayoutInfoState.value != null
        val shouldScroll = if (isScrolling) {
            mainText.width + STOP_SCROLL_HYSTERESIS_PX > constraints.maxWidth
        } else {
            mainText.width + START_SCROLL_TOLERANCE_PX >= constraints.maxWidth
        }
        if (!shouldScroll) {
            textLayoutInfoState.value = null
        } else {
            val spacing = constraints.maxWidth * 1 / 4
            textLayoutInfoState.value = TextLayoutInfo(
                textWidth = mainText.width + spacing,
                containerWidth = constraints.maxWidth
            )
            val secondTextOffset = mainText.width + offset + spacing
            val secondTextSpace = constraints.maxWidth - secondTextOffset
            if (secondTextSpace > 0) {
                secondPlaceableWithOffset = subcompose("SecondaryText") {
                    createText(Modifier)
                }.first().measure(infiniteWidthConstraints) to secondTextOffset
            } else {
                // 第二份文本不可见/不需要时显式清空 SecondaryText slot，避免旧 slot 残留
                subcompose("SecondaryText") {}
            }
        }

        layout(
            width = constraints.maxWidth,
            height = mainText.height
        ) {
            mainText.place(if (shouldScroll) offset else 0, 0)
            secondPlaceableWithOffset?.let {
                it.first.place(it.second, 0)
            }
            gradient?.place(0, 0)
        }
    }
}

private data class TextLayoutInfo(val textWidth: Int, val containerWidth: Int)

/**
 * 启动滚动的容差（像素）。
 * 当 mainText.width + 该值 >= 容器宽度时即启动滚动，避免文本宽度恰等于或仅略小于容器宽度时
 * 视觉上已贴边却不滚动的体验问题。
 */
private const val START_SCROLL_TOLERANCE_PX = 4

/**
 * 滚动状态滞回阈值（像素）。
 * 已经在滚动时，只有当容器比文本宽出该值以上才切回静态，避免临界宽度抖动反复重启动画。
 */
private const val STOP_SCROLL_HYSTERESIS_PX = 24