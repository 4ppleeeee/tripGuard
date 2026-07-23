package com.tencent.news.core.compose.view.alphavideo

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * 透明视频播放状态密封类
 *
 * 用于 Native → Compose 的状态通信。
 * Native 侧通过 JSON 编码状态，Compose 侧通过 decode 解码为具体状态类。
 */
sealed class QnAlphaVideoPlayState {

    /**
     * 编码为 JSON，供 Bridge 层传递给 Event
     */
    internal fun encode(): JSONObject {
        val json = JSONObject()
        return when (this) {
            is QnAlphaVideoPreparedState -> json.apply { put(KEY_PLAY_STATE, ON_PREPARED) }
            is QnAlphaVideoStartState -> json.apply { put(KEY_PLAY_STATE, ON_START) }
            is QnAlphaVideoPauseState -> json.apply { put(KEY_PLAY_STATE, ON_PAUSE) }
            is QnAlphaVideoStopState -> json.apply { put(KEY_PLAY_STATE, ON_STOP) }
            is QnAlphaVideoCompleteState -> json.apply { put(KEY_PLAY_STATE, ON_COMPLETE) }
            is QnAlphaVideoErrorState -> json.apply {
                put(KEY_PLAY_STATE, ON_ERROR)
                put(KEY_ERROR_CODE, code)
                put(KEY_ERROR_MESSAGE, message)
            }
            is QnAlphaVideoRenderReleasedState -> json.apply { put(KEY_PLAY_STATE, ON_RENDER_RELEASED) }
        }
    }

    companion object {
        const val ON_PREPARED = 1
        const val ON_START = 2
        const val ON_PAUSE = 3
        const val ON_STOP = 4
        const val ON_COMPLETE = 5
        const val ON_ERROR = 6
        const val ON_RENDER_RELEASED = 7

        private const val KEY_PLAY_STATE = "playState"
        private const val KEY_ERROR_CODE = "errorCode"
        private const val KEY_ERROR_MESSAGE = "errorMessage"

        /**
         * 从 Native 侧传入的 JSON 参数解码为播放状态
         */
        internal fun decode(params: Any?): QnAlphaVideoPlayState? {
            if (params !is JSONObject) return null
            val state = params.optInt(KEY_PLAY_STATE)
            if (state < 0) return null

            return when (state) {
                ON_PREPARED -> QnAlphaVideoPreparedState()
                ON_START -> QnAlphaVideoStartState()
                ON_PAUSE -> QnAlphaVideoPauseState()
                ON_STOP -> QnAlphaVideoStopState()
                ON_COMPLETE -> QnAlphaVideoCompleteState()
                ON_ERROR -> QnAlphaVideoErrorState(
                    code = params.optInt(KEY_ERROR_CODE),
                    message = params.optString(KEY_ERROR_MESSAGE)
                )
                ON_RENDER_RELEASED -> QnAlphaVideoRenderReleasedState()
                else -> null
            }
        }
    }
}

/** 视频数据加载完成，准备就绪 */
class QnAlphaVideoPreparedState : QnAlphaVideoPlayState()

/** 开始播放 */
class QnAlphaVideoStartState : QnAlphaVideoPlayState()

/** 暂停播放 */
class QnAlphaVideoPauseState : QnAlphaVideoPlayState()

/** 停止播放 */
class QnAlphaVideoStopState : QnAlphaVideoPlayState()

/** 播放完成 */
class QnAlphaVideoCompleteState : QnAlphaVideoPlayState()

/** 播放出错 */
class QnAlphaVideoErrorState(
    val code: Int,
    val message: String? = null
) : QnAlphaVideoPlayState()

/** 底层渲染资源已释放，需重新初始化 */
class QnAlphaVideoRenderReleasedState : QnAlphaVideoPlayState()
