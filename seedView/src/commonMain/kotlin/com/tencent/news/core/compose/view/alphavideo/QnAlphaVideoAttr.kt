package com.tencent.news.core.compose.view.alphavideo

import com.tencent.kuikly.core.base.Attr

/**
 * 透明视频播放器属性常量
 */
object QnAlphaVideoAttr {
    /** 注册播放状态监听器 */
    const val SET_PLAY_STATE_LISTENER = "setAlphaVideoPlayStateListener"

    /** 视频数据 */
    const val DATA = "alphaVideoData"

    /** Compose 内部状态对象（触发 Invoker 绑定） */
    internal const val STATE = "alphaVideoState"
}

/**
 * 透明视频播放器 Attr
 *
 * 扩展 with() 方法，在设置 STATE 时自动注册播放状态监听器到 Event。
 */
internal class QnAlphaVideoViewAttr : Attr() {

    /**
     * 设置属性
     *
     * 当 value 为 QnAlphaVideoState 时，自动将 playStateListener 注册到 Event，
     * 建立 Native → Compose 的回调链路。
     */
    internal fun with(key: String, value: Any): QnAlphaVideoViewAttr = this.apply {
        if (value is QnAlphaVideoState) {
            (view() as? QnAlphaVideoView)?.getViewEvent()?.let { event ->
                event.setPlayStateListener(value.playStateListener)
            }
        }
        key with value
    }
}
