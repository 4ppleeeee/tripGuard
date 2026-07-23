package com.tencent.news.core.compose.view.video

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

sealed class QnVideoPlayState {
    companion object {
        const val ON_PREPARED = 1
        const val ON_START = 2
        const val ON_START_RENDER = 3
        const val ON_PAUSE = 4
        const val ON_STOP = 5
        const val ON_COMPLETE = 6

        internal fun decode(params: Any?): QnVideoPlayState? {
            if (params !is JSONObject) return null
            val state = params.optInt("playState")
            if (state < 0) return null

            return when (state) {
                ON_PREPARED -> QnVideoPlayPreparedState()
                ON_START -> QnVideoPlayStartState()
                ON_START_RENDER -> QnVideoPlayStartRenderState()
                ON_PAUSE -> QnVideoPlayPauseState()
                ON_STOP -> QnVideoPlayStopState(
                    params.optInt("errWhat"),
                    params.optInt("errCode"),
                    params.optString("errMsg")
                )

                ON_COMPLETE -> QnVideoPlayCompleteState()
                else -> null
            }
        }
    }

    internal fun encode(): JSONObject {
        val jsonObject = JSONObject()
        return when (this) {
            is QnVideoPlayPreparedState -> jsonObject.apply {
                put("playState", ON_PREPARED)
            }

            is QnVideoPlayStartState -> jsonObject.apply {
                put("playState", ON_START)
            }

            is QnVideoPlayStartRenderState -> jsonObject.apply {
                put("playState", ON_START_RENDER)
            }

            is QnVideoPlayPauseState -> jsonObject.apply {
                put("playState", ON_PAUSE)
            }

            is QnVideoPlayStopState -> jsonObject.apply {
                put("playState", ON_STOP)
                put("errWhat", errWhat)
                put("errCode", errCode)
                put("errMsg", errMsg)
            }

            is QnVideoPlayCompleteState -> jsonObject.apply {
                put("playState", ON_COMPLETE)
            }
        }
    }
}

class QnVideoPlayPreparedState : QnVideoPlayState()
class QnVideoPlayStartState : QnVideoPlayState()
class QnVideoPlayStartRenderState : QnVideoPlayState()
class QnVideoPlayPauseState : QnVideoPlayState()
class QnVideoPlayStopState(
    val errWhat: Int,
    val errCode: Int,
    val errMsg: String? = null
) : QnVideoPlayState()

class QnVideoPlayCompleteState : QnVideoPlayState()

data class QnVideoProgressData(
    val position: Long,
    val duration: Long,
    val bufferPercent: Int
) {
    companion object {
        internal fun decode(params: Any?): QnVideoProgressData? {
            if (params !is JSONObject) return null
            return QnVideoProgressData(
                position = params.optLong("position"),
                duration = params.optLong("duration"),
                bufferPercent = params.optInt("bufferPercent")
            )
        }
    }

    internal fun encode(): JSONObject {
        return JSONObject().apply {
            put("position", position)
            put("duration", duration)
            put("bufferPercent", bufferPercent)
        }
    }
}