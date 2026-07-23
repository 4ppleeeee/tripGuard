package com.tencent.news.core.compose.view.alphavideo

import com.tencent.kuikly.core.base.event.EventHandlerFn
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.compose.view.alphavideo.invoker.AlphaVideoControllerInvoker
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.platform.api.appViewBridge
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.list.trace.AlphaVideoLog
import com.tencent.news.core.video.api.alpha.IQnAlphaVideoListener
import com.tencent.news.core.compose.view.QnViewInvokeResult
import com.tencent.news.core.video.api.alpha.IQnAlphaVideoView

/**
 * 透明视频播放器 Bridge
 *
 * 职责：
 * 1. 通过 appViewBridge() 创建平台原生透明播放器 View
 * 2. 实现 IQnAlphaVideoListener，将 Native 回调编码为 JSON 分发给 Compose 侧
 * 3. 处理 Compose → Native 的属性设置（setProp）
 * 4. 处理 Compose → Native 的方法调用（call）
 *
 * Bridge 实例由 Kuikly 框架在各平台的 RenderView 创建时实例化，
 * 与 DeclarativeBaseView 的 renderView 生命周期绑定。
 */
class QnAlphaVideoBridge(
    context: IKmmContext? = null
) : IQnAlphaVideoListener {

    private var alphaVideoView: IQnAlphaVideoView? = null

    /** 播放状态事件分发器（由 Event 层注册） */
    private var stateDispatcher: EventHandlerFn? = null

    init {
        alphaVideoView = appViewBridge()?.createAlphaVideoView(context)
        debugLog { "init, view: $alphaVideoView" }
        alphaVideoView?.setListener(this)
    }

    // ================================================================
    // IQnAlphaVideoListener 实现（Native → Compose）
    // ================================================================

    override fun onPrepared() {
        debugLog { "onPrepared" }
        dispatchState(QnAlphaVideoPlayState.ON_PREPARED)
    }

    override fun onStart() {
        debugLog { "onStart" }
        dispatchState(QnAlphaVideoPlayState.ON_START)
    }

    override fun onPause() {
        debugLog { "onPause" }
        dispatchState(QnAlphaVideoPlayState.ON_PAUSE)
    }

    override fun onStop() {
        debugLog { "onStop" }
        dispatchState(QnAlphaVideoPlayState.ON_STOP)
    }

    override fun onComplete() {
        debugLog { "onComplete" }
        dispatchState(QnAlphaVideoPlayState.ON_COMPLETE)
    }

    override fun onError(errorCode: Int, errorMessage: String?) {
        fileLog("onError code:$errorCode msg:$errorMessage")
        dispatchState(QnAlphaVideoPlayState.ON_ERROR) {
            put("errorCode", errorCode)
            put("errorMessage", errorMessage)
        }
    }

    override fun onRenderReleased() {
        debugLog { "onRenderReleased" }
        dispatchState(QnAlphaVideoPlayState.ON_RENDER_RELEASED)
    }

    /**
     * 编码状态为 JSON 并通过 stateDispatcher 分发给 Event 层
     */
    private fun dispatchState(state: Int, extra: (JSONObject.() -> Unit)? = null) {
        stateDispatcher?.invoke(JSONObject().apply {
            put("playState", state)
            extra?.invoke(this)
        }.toString())
    }

    // ================================================================
    // Compose → Native：属性设置
    // ================================================================

    fun setProp(propKey: String, propValue: Any): QnViewInvokeResult {
        when (propKey) {
            QnAlphaVideoAttr.SET_PLAY_STATE_LISTENER -> {
                val handler = (propValue as? EventHandlerFn) ?: return QnViewInvokeResult.FAIL
                stateDispatcher = handler
            }
            else -> return QnViewInvokeResult.NOT_FOUND
        }
        return QnViewInvokeResult.SUCCESS
    }

    // ================================================================
    // Compose → Native：方法调用
    // ================================================================

    fun call(method: String, params: String?, callback: ((result: Any?) -> Unit)?): QnViewInvokeResult {
        debugLog { "call method:$method, params:$params" }
        return handleCallMethod(method, params, callback)
    }

    private fun handleCallMethod(
        method: String,
        params: String?,
        callback: ((result: Any?) -> Unit)?
    ): QnViewInvokeResult {
        when (method) {
            AlphaVideoControllerInvoker.SET_PLAY_INFO -> {
                val playInfo = KtJson.safeDecode<QnAlphaVideoPlayInfo>(params)
                    ?: return QnViewInvokeResult.FAIL
                alphaVideoView?.setPlayInfo(playInfo)
            }
            AlphaVideoControllerInvoker.PLAY -> {
                alphaVideoView?.play()
            }
            AlphaVideoControllerInvoker.PAUSE -> {
                alphaVideoView?.pause()
            }
            AlphaVideoControllerInvoker.RELEASE -> {
                alphaVideoView?.release()
            }
            else -> return QnViewInvokeResult.NOT_FOUND
        }
        return QnViewInvokeResult.SUCCESS
    }

    // ================================================================
    // 生命周期
    // ================================================================

    fun onDestroy() {
        debugLog { "onDestroy" }
        alphaVideoView?.release()
        alphaVideoView = null
    }

    fun getVideoView() = alphaVideoView
}



private const val TAG = "Bridge"

private inline fun debugLog(msg: () -> String) {
    AlphaVideoLog.debug(TAG, msg)
}

private fun fileLog(msg: String) {
    AlphaVideoLog.fileLog(TAG, msg)
}