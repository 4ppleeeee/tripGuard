package com.tencent.news.core.compose.view.video.invoker

import com.tencent.news.core.compose.view.video.QnVideoState
import com.tencent.news.core.compose.view.video.QnVideoUiConfig
import com.tencent.news.core.compose.view.video.QnVideoView
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.video.api.IQnVideoUiConfig
import com.tencent.news.core.video.api.IQnVideoUiController

internal object QnVideoUiConfigInvoker {
    const val UPDATE_CONFIG = "updateConfig"
    const val SET_SCALE_TYPE = "setScaleType"
}

class QnVideoUiConfigComposeInvoker(
    private val config: IQnVideoUiConfig,
    private val scaleType: Int
) : IQnVideoUiController, IQnVideoComposeInvoker {
    var updateConfigAction: ((config: IQnVideoUiConfig) -> Unit)? = null
        set(value) {
            value?.invoke(config)
            field = value
        }

    var setScaleTypeAction: ((scaleType: Int) -> Unit)? = null
        set(value) {
            value?.invoke(scaleType)
            field = value
        }


    override fun updateConfig(config: IQnVideoUiConfig) {
        log("updateConfig config:$config")
        updateConfigAction?.invoke(config)
    }

    override fun setScaleType(scaleType: Int) {
        log("setScaleType scaleType:${scaleType}")
        setScaleTypeAction?.invoke(scaleType)
    }
}

internal class QnVideoUiConfigNativeInvoker(
    private val qnVideoView: QnVideoView
) : IQnVideoUiController, IQnVideoNativeInvoker {
    override fun updateConfig(config: IQnVideoUiConfig) {
        log("updateConfig config:$config")
        qnVideoView.callRenderViewMethod(
            methodName = QnVideoUiConfigInvoker.UPDATE_CONFIG,
            KtJson.safeEncode(config as? QnVideoUiConfig)
        )
    }

    override fun setScaleType(scaleType: Int) {
        log("setScaleType scaleType:${scaleType}")
        qnVideoView.callRenderViewMethod(
            methodName = QnVideoUiConfigInvoker.SET_SCALE_TYPE,
            params = scaleType.toString()
        )
    }

    override fun bindAction(state: QnVideoState) {
        log("bindAction")
        val invoker = state.uiConfigInvoker
        invoker.updateConfigAction = {
            updateConfig(it)
        }
        invoker.setScaleTypeAction = {
            setScaleType(it)
        }
    }
}