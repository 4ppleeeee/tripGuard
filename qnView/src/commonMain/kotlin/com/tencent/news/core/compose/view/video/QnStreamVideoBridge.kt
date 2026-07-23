package com.tencent.news.core.compose.view.video

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.compose.view.QnViewInvokeResult
import com.tencent.news.core.platform.api.VideoCreateParam
import com.tencent.news.core.platform.api.appViewBridge

/**
 * Bridge for handling QnStreamVideoView logic in Common code.
 * Delegates platform-specific view creation via [appViewBridge].
 */
class QnStreamVideoBridge(
    context: IKmmContext?,
    onViewCreated: (view: Any?) -> Unit
) {

    private var nativeView: Any? = null

    init {
        // Create view immediately with default scene
        val view = appViewBridge()?.createStreamVideoView(VideoCreateParam(context, 0))
        nativeView = view
        onViewCreated(view)
    }

    /**
     * Handle property updates from Compose/Kuikly
     */
    fun setProp(propKey: String, propValue: Any): QnViewInvokeResult {
        if ("onViewCreated" == propKey) {
            val callback = propValue as? (Any) -> Unit
            nativeView?.let { callback?.invoke(it) }
            return QnViewInvokeResult.SUCCESS
        }
        return QnViewInvokeResult.NOT_FOUND
    }

    /**
     * Handle method calls
     */
    fun call(method: String, params: Any?, callback: ((result: Any?) -> Unit)?): QnViewInvokeResult {
        // Implement method handling if needed, similar to QnVideoBridge
        return QnViewInvokeResult.NOT_FOUND
    }

    /**
     * Handle lifecycle or other cleanup
     */
    fun onDestroy() {
        // Cleanup logic
    }
}
