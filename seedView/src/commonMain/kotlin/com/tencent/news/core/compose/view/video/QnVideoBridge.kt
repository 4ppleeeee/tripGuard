package com.tencent.news.core.compose.view.video

import com.tencent.kuikly.core.base.event.EventHandlerFn
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.compose.view.QnViewInvokeResult
import com.tencent.news.core.compose.view.video.invoker.QnVideoControllerInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoDataInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoUiConfigInvoker
import com.tencent.news.core.compose.view.video.invoker.QnVideoVolumeInvoker
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeToBoolean
import com.tencent.news.core.extension.safeToInt
import com.tencent.news.core.platform.api.VideoCreateParam
import com.tencent.news.core.platform.api.appViewBridge
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.video.api.IQnDeviceMuteListener
import com.tencent.news.core.video.api.IQnVideoData
import com.tencent.news.core.video.api.IQnVideoLife
import com.tencent.news.core.video.api.IQnVideoProgressListener
import com.tencent.news.core.video.api.IQnVideoUiConfig
import com.tencent.news.core.video.api.IQnVideoView
import com.tencent.news.core.video.api.QnVideoLog

class QnVideoBridge(
    private val context: IKmmContext? = null,
    private val onVideoAfterCreate: (qnVideo: IQnVideoView?) -> Unit
) : IQnVideoLife, IQnVideoProgressListener, IQnDeviceMuteListener {

    private var qnVideo: IQnVideoView? = null
    private var lifeCycleDispatcher: EventHandlerFn? = null
    private var progressDispatcher: EventHandlerFn? = null
    private var deviceMuteDispatcher: EventHandlerFn? = null

    private fun createAndAddVideoView(scene: Int) {
        if (qnVideo != null) {
            return
        }
        qnVideo = appViewBridge()?.createVideoView(VideoCreateParam(context, scene))
        QnVideoLog.log("createAndAddVideoView, videoView:$qnVideo")
        qnVideo?.getLifeObservable()?.subscribe(this)
        qnVideo?.getProgressManager()?.registerListener(this)
        qnVideo?.getDeviceMuteManager()?.registerListener(this)
        onVideoAfterCreate(qnVideo)
    }

    override fun onVideoPrepared() {
        super.onVideoPrepared()
        QnVideoLog.log("onVideoPrepared")
        dispatchLifeCycle(QnVideoPlayState.ON_PREPARED)

    }

    override fun onVideoStart() {
        super.onVideoStart()
        QnVideoLog.log("onVideoStart")
        dispatchLifeCycle(QnVideoPlayState.ON_START)
    }

    override fun onVideoStartRender() {
        super.onVideoStartRender()
        QnVideoLog.log("onVideoStartRender")
        dispatchLifeCycle(QnVideoPlayState.ON_START_RENDER)
    }

    override fun onVideoPause() {
        super.onVideoPause()
        QnVideoLog.log("onVideoPause")
        dispatchLifeCycle(QnVideoPlayState.ON_PAUSE)
    }

    override fun onVideoStop(errWhat: Int, errCode: Int, errMsg: String?) {
        super.onVideoStop(errWhat, errCode, errMsg)
        QnVideoLog.log("onVideoStop errWhat:${errWhat}, errCode:${errCode}, errMsg:${errMsg}")
        dispatchLifeCycle(QnVideoPlayState.ON_STOP) {
            put("errWhat", errWhat)
            put("errCode", errCode)
            put("errMsg", errMsg)
        }
    }

    override fun onVideoComplete() {
        super.onVideoComplete()
        QnVideoLog.log("onVideoComplete")
        dispatchLifeCycle(QnVideoPlayState.ON_COMPLETE)
    }

    override fun onDeviceMuteChange(isMute: Boolean) {
        QnVideoLog.log("onDeviceMuteChange, isMute:${isMute}")
        deviceMuteDispatcher?.invoke(JSONObject().apply {
            this.put("isMute", isMute)
        }.toString())
    }

    private fun dispatchLifeCycle(state: Int, extraData: (JSONObject.() -> Unit)? = null) {
        lifeCycleDispatcher?.invoke(JSONObject().apply {
            this.put("playState", state)
            extraData?.invoke(this)
        }.toString())
    }

    override fun onProgress(position: Long, duration: Long, bufferPercent: Int) {
        progressDispatcher?.invoke(
            QnVideoProgressData(
                position,
                duration,
                bufferPercent
            ).encode().toString()
        )
    }

    fun setProp(propKey: String, propValue: Any): QnViewInvokeResult {
        when (propKey) {
            QnVideoAttr.SET_VIDEO_PLAY_STATE_LISTENER -> {
                val handler = (propValue as? EventHandlerFn) ?: return QnViewInvokeResult.FAIL
                lifeCycleDispatcher = handler
            }

            QnVideoAttr.SET_VIDEO_PROGRESS_LISTENER -> {
                val handler = (propValue as? EventHandlerFn) ?: return QnViewInvokeResult.FAIL
                progressDispatcher = handler
            }

            QnVideoAttr.SET_DEVICE_MUTE_LISTENER -> {
                val handler = (propValue as? EventHandlerFn) ?: return QnViewInvokeResult.FAIL
                deviceMuteDispatcher = handler
            }

            QnVideoAttr.SCENE -> {
                val scene = (propValue as? Int) ?: return QnViewInvokeResult.FAIL
                createAndAddVideoView(scene)
            }

            else -> return QnViewInvokeResult.NOT_FOUND
        }
        return QnViewInvokeResult.SUCCESS
    }

    fun call(
        method: String,
        params: String?,
        callback: ((result: Any?) -> Unit)?
    ): QnViewInvokeResult {
        return handleCallMethod(method, params, callback)
    }

    fun call(
        method: String,
        params: Any?,
        callback: ((result: Any?) -> Unit)?
    ): QnViewInvokeResult {
        return handleCallMethod(method, params, callback)
    }

    private fun handleCallMethod(
        method: String,
        params: Any?,
        callback: ((result: Any?) -> Unit)?
    ): QnViewInvokeResult {
        QnVideoLog.log("VideoView native call method:${method}, params:${params}")
        when (method) {
            QnVideoControllerInvoker.START -> qnVideo?.getVideoController()?.start()
            QnVideoControllerInvoker.PAUSE -> qnVideo?.getVideoController()?.pause()
            QnVideoControllerInvoker.PREPARE_AND_START -> {
                qnVideo?.getVideoController()
                    ?.prepareAndStart((params as? String).safeToBoolean())
            }

            QnVideoControllerInvoker.PREPARE -> qnVideo?.getVideoController()?.prepare()
            QnVideoControllerInvoker.RESUME -> qnVideo?.getVideoController()?.resume()
            QnVideoControllerInvoker.STOP -> qnVideo?.getVideoController()
                ?.stop((params as? String).safeToBoolean())

            QnVideoControllerInvoker.RELEASE -> qnVideo?.getVideoController()?.release()
            QnVideoControllerInvoker.RESET -> qnVideo?.getVideoController()?.reset()
            QnVideoDataInvoker.BIND_DATA -> {
                val data = KtJson.safeDecode<QnVideoData>(params as? String) as? IQnVideoData
                    ?: return QnViewInvokeResult.FAIL
                qnVideo?.getDataHandler()?.bindData(data)
            }

            QnVideoUiConfigInvoker.UPDATE_CONFIG -> {
                val config =
                    KtJson.safeDecode<QnVideoUiConfig>(params as? String) as? IQnVideoUiConfig
                        ?: return QnViewInvokeResult.FAIL
                qnVideo?.getUiController()?.updateConfig(config)
            }

            QnVideoUiConfigInvoker.SET_SCALE_TYPE -> {
                val scaleType = (params as? String).safeToInt()
                qnVideo?.getUiController()?.setScaleType(scaleType)
            }

            QnVideoVolumeInvoker.SET_MUTE -> qnVideo?.getVolumeController()
                ?.setMute((params as? String).safeToBoolean())

            else -> {
                return QnViewInvokeResult.NOT_FOUND
            }
        }
        return QnViewInvokeResult.SUCCESS
    }

    fun onDestroy() {
        qnVideo?.getProgressManager()?.unRegisterListener(this)
        qnVideo?.getLifeObservable()?.unSubscribe(this)
        qnVideo?.getVideoController()?.release()
        qnVideo?.getDeviceMuteManager()?.unRegisterListener(this)
    }
}
