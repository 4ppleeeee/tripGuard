package com.tencent.news.core.compose.view.video.invoker

import com.tencent.news.core.compose.view.video.QnVideoState
import com.tencent.news.core.video.api.QnVideoLog

typealias Action = () -> Unit

internal interface IQnVideoNativeInvoker {
    fun bindAction(state: QnVideoState)
}

internal interface IQnVideoComposeInvoker

internal fun IQnVideoComposeInvoker.log(msg: String) {
    QnVideoLog.log("业务侧 -> ComposeView $this msg:$msg")
}

internal fun IQnVideoNativeInvoker.log(msg: String) {
    QnVideoLog.log("ComposeView -> NativeView $this msg:$msg")
}

