package com.tencent.news.core.compose.view.video


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.tencent.news.core.compose.view.video.invoker.QnVideoControllerComposeInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoDataComposeInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoUiConfigComposeInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoVolumeComposeInvoker
import com.tencent.news.core.platform.AppStateChangeEvent
import com.tencent.news.core.platform.AppStateManager.appStateFlow
import com.tencent.news.core.video.api.IQnVideoController
import com.tencent.news.core.video.api.IQnVideoData
import com.tencent.news.core.video.api.IQnVideoDataHandler
import com.tencent.news.core.video.api.IQnVideoUiConfig
import com.tencent.news.core.video.api.IQnVideoUiController
import com.tencent.news.core.video.api.IQnVideoVolumeController
import com.tencent.news.core.video.api.QnNetVideoInfo
import com.tencent.news.core.video.api.QnVideoScaleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Stable
class QnVideoState(
    val scope: CoroutineScope,
    private val isRepeatPlay: Boolean = false,
    private val config: IQnVideoUiConfig,
    private val data: IQnVideoData,
    val scene: Int,
    val scaleType: Int = QnVideoScaleType.ORIGINAL_FULLSCREEN,
    private val isAutoPlay: Boolean = false,
    private val isMute: Boolean = true,
    val volumeInvoker: QnVideoVolumeComposeInvoker = QnVideoVolumeComposeInvoker(isMute), // invoker不需要外部传入，借用下by语法，简化下写法，防止state代码爆炸
    val uiConfigInvoker: QnVideoUiConfigComposeInvoker = QnVideoUiConfigComposeInvoker(config, scaleType),
    val controllerInvoker: QnVideoControllerComposeInvoker = QnVideoControllerComposeInvoker(
        isAutoPlay
    ),
    val dataInvoker: QnVideoDataComposeInvoker = QnVideoDataComposeInvoker(data)
) : IQnVideoController by controllerInvoker,
    IQnVideoUiController by uiConfigInvoker,
    IQnVideoDataHandler by dataInvoker,
    IQnVideoVolumeController by volumeInvoker {

    init {
        scope.launch {
            appStateFlow.collect {
                if (it == AppStateChangeEvent.Background) {
                    pause()
                }
            }
        }
    }

    private val _playStateFlow: MutableSharedFlow<QnVideoPlayState?> = MutableSharedFlow()
    val playStateFlow: SharedFlow<QnVideoPlayState?> = _playStateFlow.asSharedFlow()
    private val _progressFlow: MutableSharedFlow<QnVideoProgressData?> = MutableSharedFlow()
    val progressFlow: SharedFlow<QnVideoProgressData?> = _progressFlow.asSharedFlow()
    private val _deviceMuteFlow: MutableStateFlow<Boolean> = MutableStateFlow(isMute)
    val deviceMuteFlow: StateFlow<Boolean> = _deviceMuteFlow.asStateFlow()

    /**
     * vinfo 鉴权信息流（对齐 Android: TVKNetVideoInfo 回调）
     *
     * 由 playStateListener 在收到 [QnVideoNetInfoState] 时 emit；
     * 业务层（如 VideoAuthViewModel）订阅此流以驱动鉴权状态机。
     */
    private val _netVideoInfoFlow: MutableSharedFlow<QnNetVideoInfo> = MutableSharedFlow()
    val netVideoInfoFlow: SharedFlow<QnNetVideoInfo> = _netVideoInfoFlow.asSharedFlow()

    /**
     * 试看结束事件流（对齐 Android: onPermissionTimeout）
     *
     * 由 playStateListener 在收到 [QnVideoPermissionTimeoutState] 时 emit。
     */
    private val _permissionTimeoutFlow: MutableSharedFlow<Unit> = MutableSharedFlow()
    val permissionTimeoutFlow: SharedFlow<Unit> = _permissionTimeoutFlow.asSharedFlow()

    val firstPlay: MutableState<Boolean> = mutableStateOf(true)

    val playStateListener: OnVideoPlayStateListener =
        OnVideoPlayStateListener {
            scope.launch {
                _playStateFlow.emit(it as? QnVideoPlayState)
                when (it) {
                    is QnVideoPlayCompleteState -> {
                        if (isRepeatPlay) {
                            prepareAndStart()
                        } else {
                            stop(true)
                        }
                    }
                    is QnVideoPlayStartRenderState -> {
                        firstPlay.value = false
                    }
                    is QnVideoNetInfoState -> {
                        _netVideoInfoFlow.emit(it.info)
                    }
                    is QnVideoPermissionTimeoutState -> {
                        _permissionTimeoutFlow.emit(Unit)
                    }
                    else -> Unit
                }
            }
        }

    val progressListener: OnVideoProgressListener =
        OnVideoProgressListener { data ->
            scope.launch { _progressFlow.emit(data as? QnVideoProgressData) }
        }

    val deviceMuteListener: OnDeviceMuteListener =
        OnDeviceMuteListener { isMute ->
            // 更新设备静音状态其流是MutableStateFlow，切异步可能有时序问题
            // progressFlow是MutableSharedFlow，suspend fun emit(value: T)，所以这里需要切线程
            // scope.launch { _deviceMuteFlow.value = isMute }
            _deviceMuteFlow.value = isMute
        }

}

@Stable
@Serializable
data class QnVideoData(
    override val vid: String? = null,
    override val url: String? = null,
    override val coverUrl: String? = null,
    override val headTimeSec: Int = 0,
    override val tailTimeSec: Int = 0,
) : IQnVideoData

@Stable
@Serializable
data class QnVideoUiConfig(
    override val isShowControllerUi: Boolean = false,
    override val isShowMuteIcon: Boolean = false
) : IQnVideoUiConfig


fun interface OnVideoPlayStateListener {
    fun onPlayStateChange(state: QnVideoPlayState)
}

fun interface OnVideoProgressListener {
    fun onProgress(data: QnVideoProgressData)
}

fun interface OnDeviceMuteListener {
    fun onDeviceMuteChange(isMute: Boolean)
}
