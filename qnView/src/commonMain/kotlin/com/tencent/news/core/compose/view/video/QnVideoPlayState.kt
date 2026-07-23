package com.tencent.news.core.compose.view.video

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.news.core.video.api.QnNetVideoInfo

sealed class QnVideoPlayState {
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

            is QnVideoNetInfoState -> jsonObject.apply {
                put("playState", ON_NET_VIDEO_INFO)
                put("mediaVideoState", info.mediaVideoState)
                put("st", info.st)
                put("previewDurationSec", info.previewDurationSec)
                put("prePlayCountPerDay", info.prePlayCountPerDay)
                put("restPrePlayCount", info.restPrePlayCount)
                put("isPreview", info.isPreview)
            }

            is QnVideoPermissionTimeoutState -> jsonObject.apply {
                put("playState", ON_PERMISSION_TIMEOUT)
            }
        }
    }

    companion object {
        const val ON_PREPARED = 1
        const val ON_START = 2
        const val ON_START_RENDER = 3
        const val ON_PAUSE = 4
        const val ON_STOP = 5
        const val ON_COMPLETE = 6

        // 对齐 Android: ITvkVideoLifeObserver 中的 vinfo 鉴权相关回调
        const val ON_NET_VIDEO_INFO = 7
        const val ON_PERMISSION_TIMEOUT = 8

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
                ON_NET_VIDEO_INFO -> QnVideoNetInfoState(
                    info = QnNetVideoInfo(
                        mediaVideoState = params.optInt("mediaVideoState"),
                        st = params.optInt("st"),
                        previewDurationSec = params.optLong("previewDurationSec"),
                        prePlayCountPerDay = params.optInt("prePlayCountPerDay"),
                        restPrePlayCount = params.optInt("restPrePlayCount"),
                        isPreview = params.optBoolean("isPreview"),
                    )
                )
                ON_PERMISSION_TIMEOUT -> QnVideoPermissionTimeoutState()
                else -> null
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

/**
 * 视频 vinfo CGI 返回（含鉴权相关字段），对齐 Android: TVKNetVideoInfo
 */
class QnVideoNetInfoState(val info: QnNetVideoInfo) : QnVideoPlayState()

/**
 * 试看结束，对齐 Android: ITvkVideoLifeObserver.onPermissionTimeout
 */
class QnVideoPermissionTimeoutState : QnVideoPlayState()

/**
 * 视频播放进度数据。
 *
 * 单位与底层 [com.tencent.news.core.video.api.IQnVideoProgressListener] 对齐，
 * 三端宿主（Android / iOS / Harmony）已统一上报「毫秒」。
 *
 * @property position 当前播放位置（毫秒）
 * @property duration 视频总时长（毫秒），未知时为 0
 * @property bufferPercent 缓冲百分比，取值 0~100
 */
data class QnVideoProgressData(
    val position: Long,
    val duration: Long,
    val bufferPercent: Int
) {
    internal fun encode(): JSONObject {
        return JSONObject().apply {
            put("position", position)
            put("duration", duration)
            put("bufferPercent", bufferPercent)
        }
    }

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
}