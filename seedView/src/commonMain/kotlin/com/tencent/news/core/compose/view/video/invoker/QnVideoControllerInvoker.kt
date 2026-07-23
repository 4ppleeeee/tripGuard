package com.tencent.news.core.compose.view.video.invoker

import com.tencent.news.core.compose.view.video.QnVideoState
import com.tencent.news.core.compose.view.video.QnVideoView
import com.tencent.news.core.video.api.IQnVideoController

internal object QnVideoControllerInvoker {
    const val PREPARE_AND_START = "prepareAndStart"
    const val PREPARE = "prepare"
    const val START = "start"
    const val PAUSE = "pause"
    const val RESUME = "resume"
    const val STOP = "stop"
    const val RELEASE = "release"
    const val RESET = "reset"
}

class QnVideoControllerComposeInvoker(
    private val isAutoPlay: Boolean
) : IQnVideoController, IQnVideoComposeInvoker {
    var prepareAndStartAction: ((continuePlay: Boolean) -> Unit)? =
        null
        set(value) {
            if (isAutoPlay) {
                value?.invoke(false)
            }
            field = value
        }
    var prepareAction: Action? = null
    var startAction: Action? = null
    var pauseAction: Action? = null
    var resumeAction: Action? = null
    var stopAction: ((reset: Boolean) -> Unit)? = null
    var releaseAction: Action? = null
    var resetAction: Action? = null


    override fun prepareAndStart(continuePlay: Boolean) {
        log("prepareAndStart")
        prepareAndStartAction?.invoke(continuePlay)
    }

    override fun prepare() {
        log("prepare")
        prepareAction?.invoke()
    }

    override fun start() {
        log("start")
        startAction?.invoke()
    }

    override fun pause() {
        log("pause")
        pauseAction?.invoke()
    }

    override fun resume() {
        log("resume")
        resumeAction?.invoke()
    }

    override fun stop(reset: Boolean) {
        log("stop isReset:$reset")
        stopAction?.invoke(reset)
    }

    override fun release() {
        log("release")
        releaseAction?.invoke()
    }

    override fun reset() {
        log("reset")
        resetAction?.invoke()
    }
}

internal class QnVideoControllerNativeInvoker(
    private val qnVideoView: QnVideoView
) : IQnVideoController, IQnVideoNativeInvoker {

    override fun prepareAndStart(continuePlay: Boolean) {
        log("prepareAndStart")
        qnVideoView.callRenderViewMethod(
            methodName = QnVideoControllerInvoker.PREPARE_AND_START, continuePlay.toString()
        )
    }

    override fun prepare() {
        log("prepare")
        qnVideoView.callRenderViewMethod(methodName = QnVideoControllerInvoker.PREPARE)
    }

    override fun start() {
        log("start")
        qnVideoView.callRenderViewMethod(methodName = QnVideoControllerInvoker.START)
    }

    override fun pause() {
        log("pause")
        qnVideoView.callRenderViewMethod(methodName = QnVideoControllerInvoker.PAUSE)
    }

    override fun resume() {
        log("resume")
        qnVideoView.callRenderViewMethod(methodName = QnVideoControllerInvoker.RESUME)
    }

    override fun stop(reset: Boolean) {
        log("stop isReset:$reset")
        qnVideoView.callRenderViewMethod(
            methodName = QnVideoControllerInvoker.STOP,
            reset.toString()
        )
    }

    override fun release() {
        log("release")
        qnVideoView.callRenderViewMethod(methodName = QnVideoControllerInvoker.RELEASE)
    }

    override fun reset() {
        log("reset")
        qnVideoView.callRenderViewMethod(methodName = QnVideoControllerInvoker.RESET)
    }

    override fun bindAction(state: QnVideoState) {
        val invoker = state.controllerInvoker
        log("bindAction")
        invoker.prepareAndStartAction = { continuePlay ->
            prepareAndStart(continuePlay)
        }
        invoker.prepareAction = {
            prepare()
        }
        invoker.startAction = {
            start()
        }
        invoker.pauseAction = {
            pause()
        }
        invoker.resumeAction = {
            resume()
        }
        invoker.stopAction = {
            stop(it)
        }
        invoker.releaseAction = {
            release()
        }
        invoker.resetAction = {
            reset()
        }
    }
}