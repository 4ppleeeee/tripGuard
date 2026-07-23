package com.tencent.news.core.compose.view.alphavideo

import androidx.compose.runtime.Stable
import com.tencent.news.core.annotation.RestrictedApi
import com.tencent.news.core.video.api.alpha.AlphaVideoFormatType
import com.tencent.news.core.video.api.alpha.AlphaVideoScaleType
import com.tencent.news.core.compose.view.alphavideo.invoker.AlphaVideoControllerComposeInvoker
import com.tencent.news.core.video.api.alpha.IQnAlphaVideoController
import com.tencent.news.core.video.api.alpha.IQnAlphaVideoPlayInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Stable
@Serializable
data class QnAlphaVideoPlayInfo(
    override val url: String,
    override val formatType: AlphaVideoFormatType = AlphaVideoFormatType.RGB_ALPHA_2_1,
    override val scaleType: AlphaVideoScaleType = AlphaVideoScaleType.CENTER_CROP
) : IQnAlphaVideoPlayInfo

// ============================================================
// Compose 状态管理
// ============================================================

/**
 * 透明视频播放器 Compose 状态
 *
 * 持有业务侧所需的所有状态和 Invoker，
 * 通过 delegate by 语法暴露播放控制方法。
 *
 * 用法：
 * ```kotlin
 * val state = remember {
 *     QnAlphaVideoState(
 *         scope = rememberCoroutineScope(),
 *         playInfo = QnAlphaVideoPlayInfo(url = "..."),
 *         isAutoPlay = true
 *     )
 * }
 * QnAlphaVideo(modifier = Modifier, state = state)
 * ```
 */
@Stable
class QnAlphaVideoState(
    val scope: CoroutineScope,
    internal val playInfo: QnAlphaVideoPlayInfo,
    internal val isAutoPlay: Boolean = true,
    @RestrictedApi val controllerInvoker: AlphaVideoControllerComposeInvoker = AlphaVideoControllerComposeInvoker(isAutoPlay),
) : IQnAlphaVideoController by controllerInvoker {

    /**
     * 播放状态流（Native → Compose）
     *
     * 业务侧可通过 collectAsState 监听播放状态变化
     */
    val playStateFlow: MutableSharedFlow<QnAlphaVideoPlayState?> = MutableSharedFlow()

    /**
     * 播放状态监听器（由 Bridge/Event 层调用）
     */
    val playStateListener: OnAlphaVideoPlayStateListener =
        OnAlphaVideoPlayStateListener { state ->
            scope.launch {
                playStateFlow.emit(state)
            }
        }
}

/** 播放状态变化监听器 */
fun interface OnAlphaVideoPlayStateListener {
    fun onPlayStateChange(state: QnAlphaVideoPlayState)
}
