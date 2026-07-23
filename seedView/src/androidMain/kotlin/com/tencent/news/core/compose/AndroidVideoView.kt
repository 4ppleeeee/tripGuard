package com.tencent.news.core.compose

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.news.core.app.getKmmContext
import com.tencent.news.core.compose.view.QnViewInvokeResult
import com.tencent.news.core.compose.view.video.QnVideoBridge

open class AndroidVideoView(context: Context) : FrameLayout(context), IKuiklyRenderViewExport {
    private var bridge: QnVideoBridge? = null

    init {
        bridge = QnVideoBridge(context.getKmmContext()) { videoView ->
            (videoView as? View)?.let { addView(it) }
        }
    }

    override fun setProp(propKey: String, propValue: Any): Boolean {
        val result = bridge?.setProp(propKey, propValue) ?: QnViewInvokeResult.FAIL
        return when (result) {
            QnViewInvokeResult.SUCCESS -> true
            QnViewInvokeResult.FAIL -> false
            QnViewInvokeResult.NOT_FOUND -> super.setProp(propKey, propValue)
        }
    }

    // 统一处理所有call方法调用
    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        val result = bridge?.call(method, params, callback) ?: QnViewInvokeResult.FAIL
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