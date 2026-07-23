package com.tencent.news.core.compose.view.video.invoker

import com.tencent.news.core.compose.view.video.QnVideoData
import com.tencent.news.core.compose.view.video.QnVideoState
import com.tencent.news.core.compose.view.video.QnVideoView
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.video.api.IQnVideoData
import com.tencent.news.core.video.api.IQnVideoDataHandler

internal object QnVideoDataInvoker {
    const val BIND_DATA = "bindData"
}

class QnVideoDataComposeInvoker(
    private val data: IQnVideoData
) : IQnVideoDataHandler, IQnVideoComposeInvoker {
    var bindDateAction: ((data: IQnVideoData) -> Unit)? = null
        set(value) {
            value?.invoke(data)
            field = value
        }

    override fun bindData(data: IQnVideoData) {
        log("bindData data:$data")
        bindDateAction?.invoke(data)
    }
}

internal class QnVideoDataNativeInvoker(
    private val qnVideoView: QnVideoView
) : IQnVideoDataHandler, IQnVideoNativeInvoker {
    override fun bindData(data: IQnVideoData) {
        log("bindData data:$data")
        qnVideoView.callRenderViewMethod(
            methodName = QnVideoDataInvoker.BIND_DATA,
            KtJson.safeEncode(data as? QnVideoData)
        )
    }

    override fun bindAction(state: QnVideoState) {
        log("bindAction")
        val invoker = state.dataInvoker
        invoker.bindDateAction = {
            bindData(it)
        }
    }
}