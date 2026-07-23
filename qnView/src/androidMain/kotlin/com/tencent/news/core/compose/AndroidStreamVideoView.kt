package com.tencent.news.core.compose

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.news.core.app.getKmmContext
import com.tencent.news.core.compose.view.QnViewInvokeResult
import com.tencent.news.core.compose.view.video.QnStreamVideoBridge

class AndroidStreamVideoView(context: Context) : FrameLayout(context), IKuiklyRenderViewExport {

    private val bridge: QnStreamVideoBridge

    init {
        bridge = QnStreamVideoBridge(context.getKmmContext()) { view ->
            // Check if the returned object is an Android View and add it
            (view as? View)?.let { videoView ->
                if (indexOfChild(videoView) == -1) {
                    addView(
                        videoView,
                        LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            }
        }
    }

    override fun setProp(propKey: String, propValue: Any): Boolean {
        val result = bridge.setProp(propKey, propValue)
        return when (result) {
            QnViewInvokeResult.SUCCESS -> true
            QnViewInvokeResult.FAIL -> false
            QnViewInvokeResult.NOT_FOUND -> super.setProp(propKey, propValue)
        }
    }

    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        val result = bridge.call(method, params, callback)
        if (result == QnViewInvokeResult.SUCCESS || result == QnViewInvokeResult.FAIL) {
            return null
        }
        return super.call(method, params, callback)
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        val result = bridge.call(method, params, callback)
        if (result == QnViewInvokeResult.SUCCESS || result == QnViewInvokeResult.FAIL) {
            return null
        }
        return super.call(method, params, callback)
    }

    override fun onDestroy() {
        bridge.onDestroy()
        removeAllViews()
    }
}
