package com.tencent.news.core.compose.view.video

import com.tencent.kuikly.core.base.Attr

object QnVideoAttr {
    const val SET_VIDEO_PLAY_STATE_LISTENER = "setVideoPlayStateListener"
    const val SET_VIDEO_PROGRESS_LISTENER = "setVideoProgressListener"
    const val SET_DEVICE_MUTE_LISTENER = "setDeviceMuteListener"
    const val SCENE = "scene"
    // compose内部字段
    internal const val STATE = "state"
}


internal class QnVideoViewAttr : Attr() {
    internal fun with(key: String, value: Any): QnVideoViewAttr = this.apply {
        if (value is QnVideoState) {
            (view() as? QnVideoView)?.getViewEvent()?.let { event ->
                event.setVideoPlayStateListener(value.playStateListener)
                event.setVideoProgressListener(value.progressListener)
                event.setDeviceMuteListener(value.deviceMuteListener)
            }
        }
        key with value
    }
}