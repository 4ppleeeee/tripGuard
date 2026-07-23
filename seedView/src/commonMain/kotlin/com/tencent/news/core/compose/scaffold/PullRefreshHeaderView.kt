package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.news.core.compose.platform.ForceNoScale
import com.tencent.news.core.compose.scaffold.modifiers.margin
import com.tencent.news.core.compose.scaffold.theme.QnColor
import com.tencent.news.core.compose.view.QnLottie
import com.tencent.news.core.compose.view.QnText
import com.tencent.news.core.platform.FrameworkLottie


// ════════════════════════════════════════════════════════════════
// 共享下拉刷新指示器视图
// ════════════════════════════════════════════════════════════════

// 默认下拉距离（头部组件的 text + distance + lottie 高度）
val DEFAULT_REFRESH_THRESHOLD = 94.dp

// 下拉刷新 lottie 高度（对齐 PullHeadView 中 earth 的高度 50dp）
private val PULL_LOTTIE_HEIGHT = 50.dp

// 文字高度估算 + lottie 与文字间距（对齐 PullHeadView 中 DISTANCE = 30dp）
private val PULL_LOTTIE_SCALE_START_OFFSET = 30.dp

/**
 * 下拉刷新组件（VerticalPager 和瀑布流共用）
 */
@Composable
internal fun PullRefreshHeaderView(
    pullProgress: Float,
    isRefreshing: Boolean,
    refreshThreshold: Dp,
    showText: Boolean = true,
    foregroundColor: Color? = null,
    lottieTintColor: Color? = null,
) {
    val textColor = foregroundColor ?: QnColor.t4

    // 对齐 PullHeadView：lottie 缩放起始高度 = 文字高度估算 + DISTANCE(30dp)
    // 文字高度约 14sp ≈ 14dp，加上 DISTANCE 30dp，约 44dp
    // scaleStartHeight / refreshThreshold 即为缩放起始进度
    val lottieScaleStartProgress = remember(refreshThreshold, showText) {
        if (!showText) {
            return@remember 0f
        }
        val textHeightDp = 14f // 文字高度估算（dp）
        val distanceDp = PULL_LOTTIE_SCALE_START_OFFSET.value
        val scaleStartHeightDp = textHeightDp + distanceDp
        (scaleStartHeightDp / refreshThreshold.value).coerceIn(0f, 1f)
    }

    // 计算 lottie 缩放比例：在 [scaleStartProgress, 1.0] 区间内从 0 → 1
    val lottieScale = if (isRefreshing) {
        1f
    } else {
        if (pullProgress <= lottieScaleStartProgress) {
            0f
        } else {
            ((pullProgress - lottieScaleStartProgress) / (1f - lottieScaleStartProgress))
                .coerceIn(0f, 1f)
        }
    }

    // 文字状态对齐 PullHeadView
    val refreshText = when {
        isRefreshing -> "正在刷新"
        pullProgress >= 1f -> "释放刷新"
        else -> "下拉刷新"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(refreshThreshold),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 5.dp) // 对齐布局中 marginBottom=D5
        ) {
            // lottie 动画：刷新中自动循环播放，否则停在第0帧并按进度缩放
            ForceNoScale {
                QnLottie(
                    modifier = Modifier
                        .height(PULL_LOTTIE_HEIGHT)
                        .width(PULL_LOTTIE_HEIGHT * lottieScale).margin(bottom = 5.dp),
                    fileName = FrameworkLottie.pullRefresh,
                    tag = "pullToRefresh",
                    autoPlay = isRefreshing,
                    infinity = isRefreshing,
                    // 非刷新状态固定在第0帧；刷新中不控制进度（NaN），让动画自由播放
                    progress = if (isRefreshing) Float.NaN else 0f,
                    tintColor = lottieTintColor,
                )
            }

            if (showText) {
                QnText(
                    text = refreshText,
                    color = textColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}
