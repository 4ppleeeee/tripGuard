package com.tencent.news.core.compose.view.alphavideo

import com.tencent.kuikly.core.base.event.Event

/**
 * 透明视频播放器 Event
 *
 * 处理 Native → Compose 的事件分发。
 * Bridge 层通过 dispatchState() 将状态编码为 JSON，
 * Event 层注册 handler 解码后回调给 Compose 侧的 Flow。
 */
internal class QnAlphaVideoEvent : Event() {

    /**
     * 注册/注销播放状态监听器
     *
     * 监听器接收 JSON 字符串，解码为 QnAlphaVideoPlayState 后回调。
     */
    internal fun setPlayStateListener(listener: OnAlphaVideoPlayStateListener?) {
        if (listener != null) {
            register(QnAlphaVideoAttr.SET_PLAY_STATE_LISTENER) {
                val data = QnAlphaVideoPlayState.decode(it) ?: return@register
                listener.onPlayStateChange(data)
            }
        } else {
            unRegister(QnAlphaVideoAttr.SET_PLAY_STATE_LISTENER)
        }
    }
}
