package com.tencent.news.core.compose.view.video

import com.tencent.kuikly.core.base.event.Event
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

internal class QnVideoEvent : Event() {
    internal fun setVideoPlayStateListener(listener: OnVideoPlayStateListener?) {
        if (listener != null) {
            register(QnVideoAttr.SET_VIDEO_PLAY_STATE_LISTENER) {
                val data = QnVideoPlayState.decode(it) ?: return@register
                listener.onPlayStateChange(data)
            }
        } else {
            unRegister(QnVideoAttr.SET_VIDEO_PLAY_STATE_LISTENER)
        }
    }

    internal fun setVideoProgressListener(listener: OnVideoProgressListener?) {
        if (listener != null) {
            register(QnVideoAttr.SET_VIDEO_PROGRESS_LISTENER) {
                val data = QnVideoProgressData.decode(it) ?: return@register
                listener.onProgress(data)
            }
        } else {
            unRegister(QnVideoAttr.SET_VIDEO_PROGRESS_LISTENER)
        }
    }

    internal fun setDeviceMuteListener(listener: OnDeviceMuteListener?) {
        if (listener != null) {
            register(QnVideoAttr.SET_DEVICE_MUTE_LISTENER) {
                val json = it as? JSONObject
                if (json != null) {
                    listener.onDeviceMuteChange(json.optBoolean("isMute"))
                }
            }
        } else {
            unRegister(QnVideoAttr.SET_DEVICE_MUTE_LISTENER)
        }
    }
}