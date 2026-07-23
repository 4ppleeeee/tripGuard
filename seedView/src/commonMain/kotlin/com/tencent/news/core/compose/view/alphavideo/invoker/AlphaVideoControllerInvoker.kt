package com.tencent.news.core.compose.view.alphavideo.invoker

import com.tencent.news.core.annotation.RestrictedApi
import com.tencent.news.core.compose.view.alphavideo.QnAlphaVideoPlayInfo
import com.tencent.news.core.compose.view.alphavideo.QnAlphaVideoState
import com.tencent.news.core.compose.view.alphavideo.QnAlphaVideoView
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.video.api.alpha.IQnAlphaVideoController

/**
 * 播放控制方法名常量
 *
 * 与 Bridge 层的 call() 方法分发匹配
 */
internal object AlphaVideoControllerInvoker {
    const val SET_PLAY_INFO = "setPlayInfo"
    const val PLAY = "play"
    const val PAUSE = "pause"
    const val RELEASE = "release"
}

/**
 * 播放控制 ComposeInvoker
 *
 * 持有 action 闭包，默认为 null。
 * NativeInvoker.bindAction() 时注入实际实现。
 */
class AlphaVideoControllerComposeInvoker(
    private val isAutoPlay: Boolean
) : IQnAlphaVideoController, IAlphaVideoComposeInvoker {

    // 自动播放是否已消费（仅用于 setter 的“首次自动触发”）
    private var autoPlayConsumed = false

    var playAction: Action? = null
        set(value) {
            field = value
            if (isAutoPlay && !autoPlayConsumed) {
                autoPlayConsumed = true
                value?.invoke()
            }
        }
    var pauseAction: Action? = null
    var releaseAction: Action? = null

    override fun play() {
        log("play")
        playAction?.invoke()
    }

    override fun pause() {
        log("pause")
        pauseAction?.invoke()
    }

    override fun release() {
        log("release")
        releaseAction?.invoke()
    }

    internal fun resetAutoPlayConsumed() {
        autoPlayConsumed = false
    }
}

/**
 * 播放控制 NativeInvoker
 *
 * 持有 DeclarativeBaseView 引用，每个方法通过 callRenderViewMethod
 * 转发到 Bridge 层，Bridge 层再调用平台原生 View。
 */
internal class AlphaVideoControllerNativeInvoker(
    private val qnAlphaVideoView: QnAlphaVideoView
) : IQnAlphaVideoController, IAlphaVideoNativeInvoker {
    private var lastPlayInfoPayload: QnAlphaVideoPlayInfo? = null

    @OptIn(RestrictedApi::class)
    override fun bindAction(state: QnAlphaVideoState) {
        log("bindAction")
        val invoker = state.controllerInvoker
        val playInfo = state.playInfo
        // 仅当 playInfo 变化时才重新下发，避免重组重复触发
        if (playInfo != lastPlayInfoPayload) {
            setPlayInfo(playInfo)
            lastPlayInfoPayload = playInfo
            invoker.resetAutoPlayConsumed()
        }
        invoker.playAction = { play() }
        invoker.pauseAction = { pause() }
        invoker.releaseAction = { release() }
    }

    override fun play() {
        log("play")
        qnAlphaVideoView.callRenderViewMethod(
            methodName = AlphaVideoControllerInvoker.PLAY
        )
    }

    override fun pause() {
        log("pause")
        qnAlphaVideoView.callRenderViewMethod(
            methodName = AlphaVideoControllerInvoker.PAUSE
        )
    }

    override fun release() {
        log("release")
        qnAlphaVideoView.callRenderViewMethod(
            methodName = AlphaVideoControllerInvoker.RELEASE
        )
    }

    private fun setPlayInfo(playInfo: QnAlphaVideoPlayInfo) {
        log("init url:${playInfo.url}")
        qnAlphaVideoView.callRenderViewMethod(
            methodName = AlphaVideoControllerInvoker.SET_PLAY_INFO,
            KtJson.safeEncode(playInfo)
        )
    }
}
