package com.tencent.news.core.compose

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.news.core.app.getKmmContext
import com.tencent.news.core.compose.view.QnViewInvokeResult
import com.tencent.news.core.compose.view.alphavideo.QnAlphaVideoBridge

/**
 * Android 端透明视频播放器 RenderView
 *
 * 实现 IKuiklyRenderViewExport，持有 QnAlphaVideoBridge，
 * 参考 AndroidVideoView 模式。
 */
open class AndroidAlphaVideoView(context: Context) : FrameLayout(context), IKuiklyRenderViewExport {
    private var bridge: QnAlphaVideoBridge? = null

    init {
        bridge = QnAlphaVideoBridge(context.getKmmContext())
        (bridge?.getVideoView() as? View)?.let { addView(it) }
    }

    override fun setProp(propKey: String, propValue: Any): Boolean {
        val result = bridge?.setProp(propKey, propValue) ?: QnViewInvokeResult.FAIL
        return when (result) {
            QnViewInvokeResult.SUCCESS -> true
            QnViewInvokeResult.FAIL -> false
            QnViewInvokeResult.NOT_FOUND -> super.setProp(propKey, propValue)
        }
    }

    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        val result = bridge?.call(method, params as? String, callback) ?: QnViewInvokeResult.FAIL
        if (result == QnViewInvokeResult.SUCCESS || result == QnViewInvokeResult.FAIL) {
            return null
        }
        return super.call(method, params, callback)
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        val result = bridge?.call(method, params, callback) ?: QnViewInvokeResult.FAIL
        if (result == QnViewInvokeResult.SUCCESS || result == QnViewInvokeResult.FAIL) {
            return null
        }
        return super.call(method, params, callback)
    }

    override fun onDestroy() {
        bridge?.onDestroy()
    }
}
