package com.tencent.news.core.compose.view.video.invoker

import com.tencent.news.core.compose.view.video.QnVideoState
import com.tencent.news.core.compose.view.video.QnVideoView
import com.tencent.news.core.video.api.IQnVideoVolumeController


internal object QnVideoVolumeInvoker {
    const val SET_MUTE = "setMute"
}

class QnVideoVolumeComposeInvoker(
    private val isMute: Boolean
) : IQnVideoVolumeController,
    IQnVideoComposeInvoker {
    var setMuteAction: ((isMute: Boolean) -> Unit)? = null
        set(value) {
            value?.invoke(isMute)
            field = value
        }

    override fun setMute(isMute: Boolean) {
        log("setMute isMute:$isMute")
        setMuteAction?.invoke(isMute)
    }
}

internal class QnVideoVolumeNativeInvoker(
    private val qnVideoView: QnVideoView
) : IQnVideoVolumeController, IQnVideoNativeInvoker {

    override fun setMute(isMute: Boolean) {
        log("setMute isMute:$isMute")
        qnVideoView.callRenderViewMethod(
            methodName = QnVideoVolumeInvoker.SET_MUTE,
            isMute.toString()
        )
    }

    override fun bindAction(state: QnVideoState) {
        log("bindAction")
        val invoker = state.volumeInvoker
        invoker.setMuteAction = {
            setMute(it)
        }
    }
}